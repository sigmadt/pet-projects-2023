package ru.itmo.java.client.processor;

import ru.itmo.java.exception.TorrentException;
import ru.itmo.java.message.tracker.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class TorrentTrackerProcessor extends Processor {
    @Override
    public void connect(InetAddress ip, int port) throws IOException {
        socket = new Socket(ip, port);
    }

    public synchronized List<FileData> processListQuery() throws Exception {
        var listRequest = ListRequest.newBuilder().build();
        var trackerRequest = TrackerRequest.newBuilder()
                .setListRequest(listRequest)
                .build();

        executeWriteTask(() -> trackerRequest.writeDelimitedTo(socket.getOutputStream()));

        var trackerResponse = TrackerResponse.parseDelimitedFrom(socket.getInputStream());

        if (trackerResponse == null) {
            throw new TorrentException("Tracker server is not responding, check that it is running");
        }

        if (!trackerResponse.hasListResponse()) {
            throw new RuntimeException("no list response was sent");
        }

        return trackerResponse.getListResponse().getFileList();
    }


    public synchronized int processUploadQuery(String fileName, long fileSize) throws Exception {
        var fileData = FileData.newBuilder()
                .setName(fileName)
                .setSize(fileSize)
                .build();

        var uploadRequest = UploadRequest.newBuilder()
                .setFile(fileData)
                .build();

        var trackerRequest = TrackerRequest.newBuilder()
                .setUploadRequest(uploadRequest)
                .build();

        executeWriteTask(() -> trackerRequest.writeDelimitedTo(socket.getOutputStream()));


        var trackerResponse = TrackerResponse.parseDelimitedFrom(socket.getInputStream());

        if (trackerResponse == null) {
            throw new TorrentException("Tracker server is not responding, check that it is running");
        }

        if (!trackerResponse.hasUploadResponse()) {
            throw new RuntimeException("no upload response was sent");
        }

        return trackerResponse.getUploadResponse().getId();
    }

    public List<ClientData> processSourceQuery(int id) throws Exception {
        var sourceRequest = SourceRequest.newBuilder()
                .setId(id).build();

        var trackerRequest = TrackerRequest.newBuilder()
                .setSourceRequest(sourceRequest)
                .build();

        executeWriteTask(() -> trackerRequest.writeDelimitedTo(socket.getOutputStream()));

        var futureSourceResponse = readPool.submit(
                () -> {
                    try {
                        var trackerResponse = TrackerResponse.parseDelimitedFrom(socket.getInputStream());

                        if (!trackerResponse.hasSourceResponse()) {
                            throw new RuntimeException("no source response was sent");
                        }
                        return trackerResponse.getSourceResponse().getClientList();

                    } catch (IOException ignored) {
                    }

                    return null;
                }
        );

        var res = futureSourceResponse.get();

        if (res == null) {
            throw new RuntimeException("'source' response returned null");
        }

        return res;
    }


    public synchronized boolean processUpdateQuery(int clientPort, int count, List<Integer> fileIds) throws Exception {
        var updateRequest = UpdateRequest.newBuilder()
                .setPort(clientPort)
                .setCount(count)
                .addAllId(fileIds)
                .build();

        var trackerRequest = TrackerRequest.newBuilder()
                .setUpdateRequest(updateRequest)
                .build();

        executeWriteTask(() -> trackerRequest.writeDelimitedTo(socket.getOutputStream()));


        var trackerResponse = TrackerResponse.parseDelimitedFrom(socket.getInputStream());

        if (trackerResponse == null) {
            throw new TorrentException("Tracker server is not responding, check that it is running");
        }

        if (!trackerResponse.hasUpdateResponse()) {
            throw new RuntimeException("no update response was sent");
        }

        return trackerResponse.getUpdateResponse().getStatus();
    }


}
