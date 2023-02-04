package ru.itmo.java.client.download;

import ru.itmo.java.client.TorrentClient;
import ru.itmo.java.client.TorrentFileManager;
import ru.itmo.java.client.peer.TorrentP2PServer;
import ru.itmo.java.client.processor.TorrentP2PProcessor;
import ru.itmo.java.client.processor.TorrentTrackerProcessor;
import ru.itmo.java.exception.TorrentException;
import ru.itmo.java.message.tracker.ClientData;
import ru.itmo.java.utils.Utils;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class DownloadFileTask implements Runnable {
    private final int N_PARALLEL_PARTS;
    private final int fileId;
    private final long fileSize;
    private final Path filePath;

    private final TorrentFileManager fileManager;

    private final TorrentTrackerProcessor trackerProcessor;

    private final TorrentP2PServer p2pServer;

    private final ExecutorService downloader;


    public DownloadFileTask(int fileId, long fileSize, Path filePath, TorrentClient client, int nThreads) {
        this.fileId = fileId;
        this.fileSize = fileSize;
        this.filePath = filePath;

        this.fileManager = client.getTorrentFileManager();
        this.trackerProcessor = client.getTrackerProcessor();
        this.p2pServer = client.getP2pServer();

        this.N_PARALLEL_PARTS = nThreads;
        this.downloader = client.getPartDownloader();

    }

    private Map<Integer, ClientData> getPartToClient(int fileId, Set<Integer> excludingParts, List<ClientData> clients) throws TorrentException {
        Map<Integer, List<ClientData>> res = new HashMap<>();
        boolean noSuccess = true;

        for (var clientData : clients) {
            try {
                var availableParts = requestClientAvailableParts(fileId, clientData);
                if (availableParts.isEmpty()) {
                    continue;
                }

                availableParts.removeAll(excludingParts);

                availableParts.forEach(
                        part -> {
                            res.putIfAbsent(part, new ArrayList<>());
                            res.get(part).add(clientData);
                        }
                );
                noSuccess = false;
            } catch (TorrentException e) {
//                System.out.printf(
//                        "Could not connect to client [%s | %d], skipping... %n MESSAGE [%s]",
//                        Utils.prettyPrintIpAddress(clientData.getIp().toByteArray()),
//                        clientData.getPort(),
//                        e.getMessage());
            }
        }
        if (noSuccess) {
            throw new TorrentException("connection is failed for all clients");
        }

        return res
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> Utils.getRandomElement(e.getValue())));
    }

    private Set<Integer> requestClientAvailableParts(int fileId, ClientData clientData) throws TorrentException {
        try {
            var ip = clientData.getIp().toByteArray();
            var ipAddress = InetAddress.getByAddress(ip);

            var p2p = new TorrentP2PProcessor(ipAddress, clientData.getPort());

            var res = new HashSet<>(p2p.processStatQuery(fileId));
            return res;

        } catch (Exception e) {
            throw new TorrentException(e);
        }
    }

    private byte[] requestClientPartContent(int fileId, int partId, ClientData clientData) throws TorrentException {
        try {
            var ip = clientData.getIp().toByteArray();
            var ipAddress = InetAddress.getByAddress(ip);
            var p2p = new TorrentP2PProcessor(ipAddress, clientData.getPort());

            var res = p2p.processGetQuery(fileId, partId).toByteArray();
            return res;

        } catch (Exception e) {
            throw new TorrentException(e);
        }
    }


    @Override
    public void run() {
        try {
            var currN = 0;
            var nParts = (int) Utils.calculateNParts(fileSize);

            System.out.println();
            while (true) {
                System.out.println(Utils.drawProgressBar(currN, nParts));
                if (currN == nParts) {
                    break;
                }

                var alreadyHaveParts = fileManager.getFilePartsSet(fileId);
                var clientsWithFile = trackerProcessor.processSourceQuery(fileId);
                var partToClient = getPartToClient(fileId, alreadyHaveParts, clientsWithFile);

                if (partToClient.isEmpty()) {
                    System.out.println("Download could not be finished: no clients with needed parts...");
                    return;
                }

                downloadParts(partToClient);
                currN = fileManager.getFilePartsSet(fileId).size();
            }

            sendUpdateOnServer();
        } catch (Exception ignored) {
        }

    }


    private void downloadParts(Map<Integer, ClientData> partToClient) throws TorrentException {
        List<Integer> parts = new ArrayList<>(partToClient.keySet());
        Collections.shuffle(parts);

        var partFutures =
                parts
                        .stream()
                        .limit(N_PARALLEL_PARTS)
                        .map(
                                partId -> downloader.submit(
                                        () -> {
                                            // System.out.printf("== Working with part N%d ==%n", partId);
                                            var clientData = partToClient.get(partId);
                                            try {
                                                var buf = requestClientPartContent(fileId, partId, clientData);
                                                fileManager.writePartContent(fileId, fileSize, filePath, partId, buf);
                                                // System.out.printf("== Finish working with part N%d ==%n", partId);
                                            } catch (TorrentException e) {
//                                                System.out.println("Exception happened inside downloadParts " + e.getMessage());
                                            }
//                                            System.out.printf("Finish with part %d taken from %s %n", partId, Utils.prettyPrintClientData(clientData));
                                        }
                                )
                        ).toList();

        boolean noParts = true;

        for (var future : partFutures) {
            try {
                future.get();
                noParts = false;
            } catch (Exception ignored) {
            }
        }

        if (noParts) {
            throw new TorrentException("No parts were written");
        }
    }

    private void sendUpdateOnServer() {
        var files = List.copyOf(fileManager.getTorrentFiles());

        try {
            trackerProcessor.processUpdateQuery(
                    p2pServer.getPort(),
                    files.size(),
                    files
            );
        } catch (Exception ignored) {
        }
    }

}
