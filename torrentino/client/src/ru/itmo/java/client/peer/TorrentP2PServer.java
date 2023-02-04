package ru.itmo.java.client.peer;

import ru.itmo.java.client.TorrentFileManager;
import ru.itmo.java.server.Server;

public class TorrentP2PServer extends Server {
    public TorrentP2PServer(int port, TorrentFileManager torrentFileManager) {
        super(port, "P2P");
        setHandler((socket) -> new TorrentP2PHandler(socket, torrentFileManager));
    }

}
