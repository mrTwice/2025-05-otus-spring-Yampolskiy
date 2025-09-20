package ru.otus.hw.exceptions;

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION, message, cause);
    }
}