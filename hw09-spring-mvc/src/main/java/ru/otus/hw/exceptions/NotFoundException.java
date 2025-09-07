package ru.otus.hw.exceptions;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(ErrorCode.NOT_FOUND, message, cause);
    }
}