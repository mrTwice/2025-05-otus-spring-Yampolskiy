package ru.otus.hw.exceptions;

public class DeletionNotAllowedException extends BusinessException {
    public DeletionNotAllowedException(String message) {
        super(ErrorCode.DELETION_NOT_ALLOWED, message);
    }

    public DeletionNotAllowedException(String message, Throwable cause) {
        super(ErrorCode.DELETION_NOT_ALLOWED, message, cause);
    }
}
