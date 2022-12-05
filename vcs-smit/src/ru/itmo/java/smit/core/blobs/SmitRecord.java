package ru.itmo.java.smit.core.blobs;

import java.io.Serializable;

public class SmitRecord implements Serializable {
    private final String hash;
    private final SmitStagedStatus status;
    private final String path;

    public SmitRecord(String hash, SmitStagedStatus status, String path) {
        this.hash = hash;
        this.status = status;
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public SmitStagedStatus getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }
}
