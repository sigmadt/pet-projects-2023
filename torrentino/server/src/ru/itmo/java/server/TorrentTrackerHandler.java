package ru.itmo.java.server;

import com.google.protobuf.ByteString;
import ru.itmo.java.message.tracker.*;
import ru.itmo.java.utils.TimeConstant;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TorrentTrackerHandler implements Runnable {

    private final Socket socket;

    private final List<FileData> files;

    private final AtomicInteger nextId;

    private final Map<ClientData, ClientRecord> clients;


    public TorrentTrackerHandler(Socket socket, List<FileData> files,
                                 Map<ClientData, ClientRecord> clients,
                                 ScheduledExecutorService remover,
                                 AtomicInteger nextId) {
        this.socket = socket;
        this.files = files;
        this.clients = clients;
        this.nextId = nextId;

        remover.scheduleAtFixedRate(new UpdateTask(), 0, TimeConstant.CHECK_UPDATE, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            while (true) {
                var trackerRequest = TrackerRequest.parseDelimitedFrom(socket.getInputStream());
                if (trackerRequest == null) {
                    break;
                }

                if (trackerRequest.hasListRequest()) {
                    handleListRequest();
                } else if (trackerRequest.hasUploadRequest()) {
                    handleUploadRequest(trackerRequest.getUploadRequest());
                } else if (trackerRequest.hasSourceRequest()) {
                    handleSourceRequest(trackerRequest.getSourceRequest());
                } else if (trackerRequest.hasUpdateRequest()) {
                    handleUpdateRequest(trackerRequest.getUpdateRequest());
                }
            }
        } catch (IOException ignored) {}
    }

    private void handleListRequest() throws IOException {
        var listResponse = ListResponse.newBuilder()
                .addAllFile(files)
                .build();

        TrackerResponse.newBuilder()
                .setListResponse(listResponse)
                .build()
                .writeDelimitedTo(socket.getOutputStream());
    }

    private void handleUploadRequest(UploadRequest uploadRequest) throws IOException {
        var givenFileData = uploadRequest.getFile();
        var fileId = nextId.getAndIncrement();

        var fileData = FileData.newBuilder()
                .setName(givenFileData.getName())
                .setSize(givenFileData.getSize())
                .setId(fileId)
                .build();

        files.add(fileData);

        var uploadResponse = UploadResponse.newBuilder()
                .setId(fileId).build();

        TrackerResponse.newBuilder()
                .setUploadResponse(uploadResponse)
                .build()
                .writeDelimitedTo(socket.getOutputStream());
    }


    private void handleSourceRequest(SourceRequest sourceRequest) throws IOException {
        var fileId = sourceRequest.getId();

        List<ClientData> validClients = new ArrayList<>();

        clients
                .forEach(
                        (client, record) -> {
                            if (record.getFiles().contains(fileId)) {
                                validClients.add(client);
                            }
                        }
                );

        var sourceResponse = SourceResponse.newBuilder()
                .setCount(validClients.size())
                .addAllClient(validClients)
                .build();

        TrackerResponse.newBuilder()
                .setSourceResponse(sourceResponse)
                .build()
                .writeDelimitedTo(socket.getOutputStream());


    }

    private void handleUpdateRequest(UpdateRequest updateRequest) throws IOException {
        var status = true;

        var clientPort = updateRequest.getPort();

        var fileIds = updateRequest.getIdList();

        var ip = socket.getInetAddress().getAddress();

        var client = ClientData.newBuilder()
                .setIp(ByteString.copyFrom(ip))
                .setPort(clientPort)
                .build();

        var clientRecord = new ClientRecord(System.currentTimeMillis(), Set.copyOf(fileIds));
        clients.put(client, clientRecord);

        var updateResponse = UpdateResponse.newBuilder()
                .setStatus(status)
                .build();


        TrackerResponse.newBuilder()
                .setUpdateResponse(updateResponse)
                .build()
                .writeDelimitedTo(socket.getOutputStream());
    }

    private final class UpdateTask implements Runnable {
        @Override
        public void run() {
            var currTime = System.currentTimeMillis();
            var clientRecords = clients.values();
            var oldClients =
                    clientRecords
                            .stream()
                            .filter(client -> currTime - client.getLastUpdate() > TimeConstant.TIME_OUT_UPDATE)
                            .toList();

            clientRecords.removeAll(oldClients);
        }
    }
}
