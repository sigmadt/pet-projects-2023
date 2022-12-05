package ru.itmo.java.smit.exception;

public class SmitException extends Exception {

    public SmitException() {
    }

    public SmitException(String message) {
        super(message);
    }

    public SmitException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmitException(Throwable cause) {
        super(cause);
    }

    public SmitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
