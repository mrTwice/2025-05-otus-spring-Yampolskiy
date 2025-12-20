package ru.otus.java.springframework.yampolskiy.exceptions;

public class DuplicateException extends BusinessException {
    public DuplicateException(String message) {
        super(ErrorCode.DUPLICATE, message);
    }

    public DuplicateException(String message, Throwable cause) {
        super(ErrorCode.DUPLICATE, message, cause);
    }
}