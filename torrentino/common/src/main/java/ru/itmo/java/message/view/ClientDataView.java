package ru.itmo.java.message.view;

import com.google.protobuf.ByteString;
import ru.itmo.java.message.tracker.ClientData;

import java.io.Serializable;

public class ClientDataView implements Serializable {
    private final int port;
    private final byte[] ip;

    public ClientDataView(ClientData clientData) {
        this.port = clientData.getPort();
        this.ip = clientData.getIp().toByteArray();
    }

    public int getPort() {
        return port;
    }

    public byte[] getIp() {
        return ip;
    }

    public ClientData toClientData() {
        return ClientData.newBuilder()
                .setPort(port)
                .setIp(ByteString.copyFrom(ip))
                .build();
    }
}
