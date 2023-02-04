package ru.itmo.java.client.processor;

import com.google.protobuf.ByteString;
import ru.itmo.java.message.tracker.ClientRequest;
import ru.itmo.java.message.tracker.ClientResponse;
import ru.itmo.java.message.tracker.GetRequest;
import ru.itmo.java.message.tracker.StatRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class TorrentP2PProcessor {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private final InetAddress ip;
    private final int port;

    public TorrentP2PProcessor(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect(InetAddress ip, int port) throws IOException {
        socket = new Socket(ip, port);

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        socket.setSoTimeout(10000);
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }


    public List<Integer> processStatQuery(int id) throws Exception {
        connect(ip, port);

        var statRequest = StatRequest.newBuilder().setId(id).build();
        var clientRequest = ClientRequest.newBuilder()
                .setStatRequest(statRequest)
                .build();

        clientRequest.writeDelimitedTo(outputStream);

        var clientResponse =
                ClientResponse.parseDelimitedFrom(inputStream);

        if (clientResponse == null) {
            throw new RuntimeException("'stat' response returned null");
        }

        if (!clientResponse.hasStatResponse()) {
            throw new RuntimeException("no stat response was sent");
        }

        var statResponse = clientResponse.getStatResponse();
        var res = statResponse.getPartList();

        close();
        return res;
    }


    public ByteString processGetQuery(int fileId, int partId) throws Exception {
        connect(ip, port);

        var getRequest = GetRequest.newBuilder()
                .setId(fileId)
                .setPart(partId)
                .build();

        var clientRequest = ClientRequest.newBuilder()
                .setGetRequest(getRequest)
                .build();


        clientRequest.writeDelimitedTo(outputStream);

        var clientResponse = ClientResponse.parseDelimitedFrom(inputStream);
        if (clientResponse == null) {
            throw new RuntimeException("'get' response returned null");
        }

        if (!clientResponse.hasGetResponse()) {
            throw new RuntimeException("no get response was sent");
        }
        var getResponse = clientResponse.getGetResponse();
        var res = getResponse.getContent();
        close();

        return res;
    }
}
