package ru.otus.java.springframework.yampolskiy.integretion.exception;

public class ModerationServiceCallException extends RuntimeException {
    public ModerationServiceCallException(String message) {
        super(message);
    }

    public ModerationServiceCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
