package ru.itmo.java.client;

import ru.itmo.java.exception.TorrentException;
import ru.itmo.java.exception.TorrentSerializationException;
import ru.itmo.java.serialization.FileHandler;
import ru.itmo.java.utils.FileConstant;
import ru.itmo.java.utils.Utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TorrentFileManager {
    private final Path storePath;
    private final Path pathFileIdToPath;
    private final Path pathFileIdToParts;
    private final Path pathFileInfo;
    private int savedPort;

    private ConcurrentHashMap<Integer, String> fileIdToPath;

    private ConcurrentHashMap<Integer, BitSet> fileIdToParts;


    public TorrentFileManager(Path workDir, int port) {
        storePath = Utils.resolveStorePathForClient(workDir);
        pathFileIdToPath = storePath.resolve("fileIdToPath");
        pathFileIdToParts = storePath.resolve("fileIdToParts");
        pathFileInfo = storePath.resolve("info");

        tryReloadPort(port);
        fileIdToPath = new ConcurrentHashMap<>();
        fileIdToParts = new ConcurrentHashMap<>();
    }

    public void storeWholeFile(int fileId, Path filePath) {
        var fileSize = filePath.toFile().length();

        fileIdToPath.put(fileId, filePath.toString());


        var parts = new BitSet();
        var nParts = (int) Utils.calculateNParts(fileSize);

        parts.set(0, nParts);
        System.out.printf("NOW PARTS for fileId=%d is %d %n", fileId, parts.cardinality());
        fileIdToParts.put(fileId, parts);
    }

    public void storeEmptyFile(int fileId, Path filePath) {
        fileIdToPath.put(fileId, filePath.toString());

        var parts = new BitSet();
        fileIdToParts.put(fileId, parts);
    }

    public void storePartOfFile(int fileId, int partId, Path filePath) {
        if (fileIdToPath.containsKey(fileId)) {
            fileIdToParts.get(fileId).set(partId);
            return;
        }
        fileIdToPath.put(fileId, filePath.toString());
        var parts = new BitSet();
        parts.set(partId);
        fileIdToParts.put(fileId, parts);
    }


    public BitSet getFileParts(int fileId) {
        return fileIdToParts.get(fileId);
    }

    public synchronized Set<Integer> getFilePartsSet(int fileId) throws TorrentException {
        if (!fileIdToPath.containsKey(fileId)) {
            throw new TorrentException("no such file");
        }

        var bs = fileIdToParts.get(fileId);
        var allParts = bs.size();
        var nParts = bs.cardinality();
        Set<Integer> res = new HashSet<>();

        if (nParts > 0) {
            for (int b = 0; b < allParts; b++) {
                if (bs.get(b)) {
                    res.add(b);
                }
            }
        }
        return Collections.unmodifiableSet(res);
    }

    public synchronized Path getFilePath(int fileId) {
        return Path.of(fileIdToPath.get(fileId));
    }

    public Set<Integer> getTorrentFiles() {
        return fileIdToParts.keySet();
    }

    public boolean isFileInTorrent(int fileId) {
        return fileIdToParts.containsKey(fileId);
    }


    public void writePartContent(int fileId, long fileSize, Path filePath, int partId, byte[] content) throws TorrentException {
        long currPos = (long) partId * FileConstant.BLOCK;
        int block = Utils.getBlockSize(partId, fileSize);

        try (var raf = new RandomAccessFile(filePath.toFile(), "rw")) {
            raf.seek(currPos);

            raf.write(content, 0, block);
            storePartOfFile(fileId, partId, filePath);
        } catch (Exception e) {
            throw new TorrentException(e);
        }

    }

    public int readPartContent(byte[] buf, Path filePath, int partId) throws IOException {
        int currPos = partId * FileConstant.BLOCK;
        var fileSize = filePath.toFile().length();
        int block = Utils.getBlockSize(partId, fileSize);

        try (var raf = new RandomAccessFile(filePath.toFile(), "rw")) {
            raf.seek(currPos);
            raf.readFully(buf, 0, block);
        } catch (EOFException ignored) {}

        return block;
    }



    public void storeInLocal() throws TorrentSerializationException {
        FileHandler.writeInt(savedPort, pathFileInfo);
        System.out.println("...Saving info about paths...");
        FileHandler.write(new HashMap<>(fileIdToPath), pathFileIdToPath);
        System.out.println("...Saving info about parts...");
        FileHandler.write(new HashMap<>(fileIdToParts), pathFileIdToParts);
    }

    public void loadFromLocal() throws TorrentSerializationException {
        if (!canLoadFromLocal()) {
            throw new RuntimeException("can't restore from session");
        }

        fileIdToPath = new ConcurrentHashMap<>(FileHandler.read(pathFileIdToPath));
        fileIdToParts = new ConcurrentHashMap<>(FileHandler.read(pathFileIdToParts));

        System.out.printf("Found paths: %d %n", fileIdToPath.size());
        System.out.printf("Found files: %d %n", fileIdToParts.size());

        fileIdToParts
                .forEach(
                        (fileId, nParts) -> System.out.printf("fileId=%d nParts=%d %n", fileId, nParts.cardinality())
                );
    }

    public boolean canLoadFromLocal() {
        return Files.exists(pathFileIdToPath) && Files.exists(pathFileIdToParts);
    }

    public Path getStoragePath() {
        return storePath;
    }

    public int getSavedPort() {
        return savedPort;
    }

    public void setSavedPort(int savedPort) {
        this.savedPort = savedPort;
    }

    public void tryReloadPort(int givenPort) {
        if (!Files.exists(pathFileInfo)) {
            savedPort = givenPort;
        }

        try {
            savedPort = FileHandler.readInt(pathFileInfo);
            System.out.println("FOUND PORT: " + savedPort);
        } catch (Exception ignored) {
            savedPort = givenPort;
        }
    }
}
