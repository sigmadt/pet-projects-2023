package ru.itmo.java.smit.core.blobs;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public enum SmitBlobStatus implements Serializable {
    STAGED_ADDED,
    STAGED_DELETED,
    STAGED_MODIFIED,
    UNTRACKED_NEW,
    UNTRACKED_DELETED,
    UNTRACKED_MODIFIED;

    private Set<String> blobs = new TreeSet<>();

    SmitBlobStatus() {}

    public Set<String> getBlobs() {
        return blobs;
    }

    public void setBlobs(Set<String> blobs) {
        this.blobs = blobs;
    }
}
