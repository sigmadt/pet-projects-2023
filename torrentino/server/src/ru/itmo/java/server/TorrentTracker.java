package ru.itmo.java.server;


import ru.itmo.java.exception.TorrentSerializationException;
import ru.itmo.java.message.tracker.ClientData;
import ru.itmo.java.message.tracker.FileData;
import ru.itmo.java.message.view.ClientDataView;
import ru.itmo.java.message.view.FileDataView;
import ru.itmo.java.serialization.FileHandler;
import ru.itmo.java.utils.Constant;
import ru.itmo.java.utils.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class TorrentTracker extends Server implements Closeable {

    private List<FileData> files = Collections.synchronizedList(new ArrayList<>());

    private Map<ClientData, ClientRecord> clients = new ConcurrentHashMap<>();

    private final ScheduledExecutorService remover = Executors.newSingleThreadScheduledExecutor();


    private AtomicInteger nextId = new AtomicInteger();


    private final Path filesPath;
    private final Path clientsPath;
    private final Path nextIdPath;

    private final Path storePath;

    public TorrentTracker() {
        this(Constant.TRACKER_PORT, Paths.get("."));
    }

    public TorrentTracker(int port, Path workDir) {
        super(port, "Tracker");

        storePath = Utils.resolveStorePathForTracker(workDir);
        filesPath = storePath.resolve("files");
        clientsPath = storePath.resolve("clients");
        nextIdPath = storePath.resolve("id");


        setHandler((socket) -> new TorrentTrackerHandler(socket, files, clients, remover, nextId));
    }


    @Override
    public void close() throws IOException {
        save();
        remover.shutdownNow();
        shutdown();
    }


    public void save() {
        try {
            if (!Files.exists(storePath)) {
                Files.createDirectories(storePath);
            }
            FileHandler.write(nextId, nextIdPath);
            System.out.println("Saved id");
            FileHandler.write(files.stream().map(FileDataView::new).toList(), filesPath);
            System.out.println("Saved available files");

            FileHandler.write(
                    clients
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    e -> new ClientDataView(e.getKey()),
                                    Map.Entry::getValue
                            )),
                    clientsPath);

            System.out.println("Saved available clients");
        } catch (Exception e) {
            System.out.println("Error happened during saving torrent state");
        }
    }

    public void load() {
        try {
            nextId = FileHandler.read(nextIdPath);

            List<FileDataView> filesView = FileHandler.read(filesPath);
            files = Collections.synchronizedList(filesView.stream().map(FileDataView::toFileData).collect(Collectors.toList()));
            Map<ClientDataView, ClientRecord> clientsView = FileHandler.read(clientsPath);
//            System.out.println("GOT: " + clientsView.size());

            clients =
                    clientsView
                            .entrySet()
                            .stream()
                            .collect(Collectors.toConcurrentMap(
                                    e -> e.getKey().toClientData(),
                                    Map.Entry::getValue
                            ));

        } catch (TorrentSerializationException e) {
            System.out.println("Error happened during loading previous torrent state");
        }
    }

    public boolean canLoad() {
        return Files.exists(nextIdPath) &&
                Files.exists(filesPath) &&
                Files.exists(clientsPath);
    }

    public List<FileData> getFiles() {
        return files;
    }

    public Map<ClientData, ClientRecord> getClients() {
        return clients;
    }
}
