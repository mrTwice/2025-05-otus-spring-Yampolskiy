package ru.otus.java.springframework.yampolskiy.exception;

public class ModerationServiceException extends RuntimeException {
    protected ModerationServiceException(String message) {
        super(message);
    }
}
