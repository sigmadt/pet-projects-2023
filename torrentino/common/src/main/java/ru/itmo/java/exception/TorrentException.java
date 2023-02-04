package ru.itmo.java.exception;

public class TorrentException extends Exception {
    public TorrentException(String message) {
        super(message);
    }

    public TorrentException(Throwable cause) {
        super(cause);
    }

    public TorrentException(String message, Throwable cause) {
        super(message, cause);
    }
}