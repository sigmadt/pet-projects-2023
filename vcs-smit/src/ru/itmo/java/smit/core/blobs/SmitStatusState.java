package ru.itmo.java.smit.core.blobs;

import java.util.TreeSet;

public class SmitStatusState {
    private final SmitBlobStatus stagedAdded = SmitBlobStatus.STAGED_ADDED;
    private final SmitBlobStatus stagedDeleted = SmitBlobStatus.STAGED_DELETED;
    private final SmitBlobStatus stagedModified = SmitBlobStatus.STAGED_MODIFIED;
    private final SmitBlobStatus untrackedNew = SmitBlobStatus.UNTRACKED_NEW;
    private final SmitBlobStatus untrackedDeleted = SmitBlobStatus.UNTRACKED_DELETED;
    private final SmitBlobStatus untrackedModified = SmitBlobStatus.UNTRACKED_MODIFIED;

    public void init() {
        stagedAdded.setBlobs(new TreeSet<>());
        stagedDeleted.setBlobs(new TreeSet<>());
        stagedModified.setBlobs(new TreeSet<>());
        untrackedNew.setBlobs(new TreeSet<>());
        untrackedDeleted.setBlobs(new TreeSet<>());
        untrackedModified.setBlobs(new TreeSet<>());
    }

    public SmitBlobStatus getStagedAdded() {
        return stagedAdded;
    }

    public SmitBlobStatus getStagedDeleted() {
        return stagedDeleted;
    }

    public SmitBlobStatus getStagedModified() {
        return stagedModified;
    }

    public SmitBlobStatus getUntrackedNew() {
        return untrackedNew;
    }

    public SmitBlobStatus getUntrackedDeleted() {
        return untrackedDeleted;
    }

    public SmitBlobStatus getUntrackedModified() {
        return untrackedModified;
    }

    public void addStagedAdded(String blobName) {
        stagedAdded.getBlobs().add(blobName);
    }

    public void addStagedDeleted(String blobName) {
        stagedDeleted.getBlobs().add(blobName);
    }

    public void addStagedModified(String blobName) {
        stagedModified.getBlobs().add(blobName);
    }

    public void addUntrackedNew(String blobName) {
        untrackedNew.getBlobs().add(blobName);
    }

    public void addUntrackedDeleted(String blobName) {
        untrackedDeleted.getBlobs().add(blobName);
    }

    public void addUntrackedModified(String blobName) {
        untrackedModified.getBlobs().add(blobName);
    }

}
