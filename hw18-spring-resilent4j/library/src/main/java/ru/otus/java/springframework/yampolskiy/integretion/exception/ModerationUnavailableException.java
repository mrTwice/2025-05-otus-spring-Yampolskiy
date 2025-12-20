package ru.otus.java.springframework.yampolskiy.integretion.exception;

import ru.otus.java.springframework.yampolskiy.exceptions.BusinessException;
import ru.otus.java.springframework.yampolskiy.exceptions.ErrorCode;

public class ModerationUnavailableException extends BusinessException {
    public ModerationUnavailableException(String message, Throwable cause) {
        super(ErrorCode.MODERATION_UNAVAILABLE, message, cause);
    }
}
