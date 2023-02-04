package ru.itmo.java.client;

import ru.itmo.java.client.download.DownloadFileTask;
import ru.itmo.java.client.peer.TorrentP2PServer;
import ru.itmo.java.client.processor.TorrentTrackerProcessor;
import ru.itmo.java.exception.TorrentException;
import ru.itmo.java.exception.TorrentSerializationException;
import ru.itmo.java.message.tracker.FileData;
import ru.itmo.java.utils.Constant;
import ru.itmo.java.utils.TimeConstant;
import ru.itmo.java.utils.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TorrentClient implements Closeable {
    private final static int DEFAULT_PORT = 0;

    private final static int N_PARALLEL_PARTS = 6;
    private final static int N_PARALLEL_FILES = 2;
    private final TorrentP2PServer p2pServer;


    private final TorrentTrackerProcessor trackerProcessor;

    private final TorrentFileManager torrentFileManager;

    private final ScheduledExecutorService updater = Executors.newSingleThreadScheduledExecutor();

    private final ExecutorService fileDownloader = Executors.newFixedThreadPool(N_PARALLEL_FILES);
    private final ExecutorService partDownloader = Executors.newFixedThreadPool(N_PARALLEL_PARTS);


    public static TorrentClient start(int port) {
        return start(port, Paths.get("."));
    }

    public static TorrentClient start(int port, Path workDir) {
        var torrentClient = new TorrentClient(port, workDir);

        try {
            if (torrentClient.canLoad()) {
                System.out.println("Found torrent client files from previous session...");
                torrentClient.load();
            } else {
                System.out.println("Initializing new session...");
            }
            return torrentClient;
        } catch (Exception e) {
            System.out.println("Can't initialize torrent client");
        }
        return null;
    }


    private TorrentClient(int port, Path workDir) {
        torrentFileManager = new TorrentFileManager(workDir, port);
        p2pServer = new TorrentP2PServer(torrentFileManager.getSavedPort(), torrentFileManager);
        trackerProcessor = new TorrentTrackerProcessor();

        try {
            p2pServer.run();
            torrentFileManager.setSavedPort(p2pServer.getPort());
        } catch (IOException e) {
            System.out.println("RUN IS BROKEN FOR P2P SERVER");
        }

    }


    public void run(InetAddress ip) throws IOException {
        trackerProcessor.connect(ip, Constant.TRACKER_PORT);
        scheduleUpdateTask();
    }

    @Override
    public void close() throws IOException {
        System.out.println("Saving client session");
        try {
            save();
        } catch (TorrentSerializationException e) {
            System.out.println("Unable to save client state");
        }

        trackerProcessor.close();
        p2pServer.shutdown();

        updater.shutdown();
    }

    public void save() throws TorrentSerializationException, IOException {
        var storePath = torrentFileManager.getStoragePath();
        if (!Files.exists(storePath)) {
            Files.createDirectories(storePath);
        }
        torrentFileManager.storeInLocal();
    }

    public void load() throws TorrentSerializationException {
        torrentFileManager.loadFromLocal();
    }

    public boolean canLoad() {
        return torrentFileManager.canLoadFromLocal();
    }


    private void updateTask() {
        try {
            int p2pPort = p2pServer.getPort();
            var fileIds = List.copyOf(torrentFileManager.getTorrentFiles());

            trackerProcessor.processUpdateQuery(p2pPort, fileIds.size(), fileIds);
        } catch (Exception e) {
            System.out.printf("Scheduled update gone wrong for port[%d] %n", p2pServer.getPort());
        }
    }

    private void scheduleUpdateTask() {
        updater.scheduleAtFixedRate(
                this::updateTask,
                0, TimeConstant.REFRESH_UPDATE, TimeUnit.MILLISECONDS
        );
    }


    public List<FileData> getFiles() throws Exception {
        return trackerProcessor.processListQuery();
    }

    public void upload(String strPath) throws TorrentException {
        var filePath = Paths.get(strPath);

        if (!Utils.isFileValid(filePath)) {
            throw new TorrentException(String.format("Invalid file for path %s", filePath));
        }

        var metaFile = filePath.toFile();
        var fileName = filePath.getFileName().toString();
        var fileSize = metaFile.length();

        try {
            // 1. Upload and get fresh id of file
            var fileId = trackerProcessor.processUploadQuery(fileName, fileSize);

            // 2. Store file in manager
            torrentFileManager.storeWholeFile(fileId, filePath);

            // 3. Update info
            var p2pPort = p2pServer.getPort();
            var fileIds = List.copyOf(torrentFileManager.getTorrentFiles());
            trackerProcessor.processUpdateQuery(p2pPort, fileIds.size(), fileIds);

        } catch (Exception e) {
            throw new TorrentException("Upload query gone wrong");
        }
    }


    public void download(int fileId, Path destinationDir) throws Exception {
        var validDir = checkDirectory(destinationDir);

        var allFiles = trackerProcessor.processListQuery();

        var fileToDownload = getFileById(fileId, allFiles);

        if (fileToDownload == null) {
            throw new TorrentException(String.format("file with id %d can not be found %n", fileId));
        }

        if (torrentFileManager.isFileInTorrent(fileId)) {
            System.out.println("file is already on local machine");
            return;
        }

        var filePath = validDir.resolve(fileToDownload.getName());
        var fileSize = fileToDownload.getSize();

        if (!torrentFileManager.isFileInTorrent(fileToDownload.getId())) {
            torrentFileManager.storeEmptyFile(fileId, filePath);
        }


        fileDownloader.submit(() -> {
            var task = new DownloadFileTask(fileId, fileSize, filePath, this, N_PARALLEL_PARTS);
            task.run();
        });
    }


    private FileData getFileById(int fileId, List<FileData> files) {
        for (var file : files) {
            if (file.getId() == fileId) {
                return file;
            }
        }
        return null;
    }

    private Path checkDirectory(Path destinationDir) throws IOException, TorrentException {
        if (!Files.isDirectory(destinationDir)) {
            throw new TorrentException(String.format("Provided %s is not a directory", destinationDir));
        }

        if (!Files.exists(destinationDir)) {
            Files.createDirectories(destinationDir);
        }

        return destinationDir;
    }

    public TorrentP2PServer getP2pServer() {
        return p2pServer;
    }


    public TorrentTrackerProcessor getTrackerProcessor() {
        return trackerProcessor;
    }

    public TorrentFileManager getTorrentFileManager() {
        return torrentFileManager;
    }

    public ExecutorService getPartDownloader() {
        return partDownloader;
    }
}
