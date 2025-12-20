package ru.otus.java.springframework.yampolskiy.exception;

public class ModerationUnavailableException extends ModerationServiceException {
    public ModerationUnavailableException(String message) {
        super(message);
    }
}
