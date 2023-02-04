package ru.itmo.java.server;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class ClientRecord implements Serializable {
    private final long lastUpdate;
    private final Set<Integer> files;

    public ClientRecord(long lastUpdate, Set<Integer> files) {
        this.lastUpdate = lastUpdate;
        this.files = files;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public Set<Integer> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return "Client Files: " + List.copyOf(files);
    }
}
