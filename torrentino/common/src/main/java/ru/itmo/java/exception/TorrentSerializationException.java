package ru.itmo.java.exception;

public class TorrentSerializationException extends Exception {
    public TorrentSerializationException(String message) {
        super(message);
    }

    public TorrentSerializationException(Throwable cause) {
        super(cause);
    }

    public TorrentSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
