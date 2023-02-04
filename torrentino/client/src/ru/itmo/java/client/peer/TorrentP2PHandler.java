package ru.itmo.java.client.peer;


import com.google.protobuf.ByteString;
import ru.itmo.java.client.TorrentFileManager;
import ru.itmo.java.message.tracker.*;
import ru.itmo.java.utils.FileConstant;

import java.io.IOException;
import java.net.Socket;

public class TorrentP2PHandler implements Runnable {
    private final Socket socket;

    private final TorrentFileManager torrentFileManager;

    public TorrentP2PHandler(Socket socket, TorrentFileManager torrentFileManager) {
        this.socket = socket;
        this.torrentFileManager = torrentFileManager;
    }

    @Override
    public void run() {
        try {
            while (true) {
                var clientRequest = ClientRequest.parseDelimitedFrom(socket.getInputStream());
                if (clientRequest == null) {
                    break;
                }
                if (clientRequest.hasStatRequest()) {
                    handleStatRequest(clientRequest.getStatRequest());
                } else if (clientRequest.hasGetRequest()) {
                    handleGetRequest(clientRequest.getGetRequest());
                }
            }
        } catch (IOException ignored) {
        }
    }

    public void handleStatRequest(StatRequest statRequest) throws IOException {
        var fileId = statRequest.getId();

        var statResponse = StatResponse.newBuilder();

        var fileParts = torrentFileManager.getFileParts(fileId);
        var allFileParts = fileParts.size();
        var nFileParts = fileParts.cardinality();

        statResponse.setCount(nFileParts);
        if (nFileParts > 0) {
            for (int partId = 0; partId < allFileParts; partId++) {
                if (fileParts.get(partId)) {
                    statResponse.addPart(partId);
                }
            }
        }

        var clientResponse =
                ClientResponse.newBuilder()
                        .setStatResponse(statResponse.build())
                        .build();

        clientResponse.writeDelimitedTo(socket.getOutputStream());
    }

    public void handleGetRequest(GetRequest getRequest) throws IOException {
        var fileId = getRequest.getId();
        var partId = getRequest.getPart();

        ByteString content = null;

        if (torrentFileManager.isFileInTorrent(fileId)) {
            byte[] buf = new byte[FileConstant.BLOCK];
            int nBytes = torrentFileManager.readPartContent(buf, torrentFileManager.getFilePath(fileId), partId);

            // System.out.println("got bytes! length: " + nBytes);
            content = ByteString.copyFrom(buf);
        }

        if (content == null) {
            return;
        }

        var getResponse = GetResponse.newBuilder()
                .setContent(content)
                .build();

        ClientResponse.newBuilder()
                .setGetResponse(getResponse)
                .build()
                .writeDelimitedTo(socket.getOutputStream());
    }
}
