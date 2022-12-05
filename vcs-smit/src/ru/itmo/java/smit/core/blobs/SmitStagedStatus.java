package ru.itmo.java.smit.core.blobs;

import java.io.Serializable;

public enum SmitStagedStatus implements Serializable {
    ADDED,
    DELETED,
    MODIFIED
}
