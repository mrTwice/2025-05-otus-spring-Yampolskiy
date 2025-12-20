package ru.otus.java.springframework.yampolskiy.integretion.exception;

import ru.otus.java.springframework.yampolskiy.exceptions.BusinessException;
import ru.otus.java.springframework.yampolskiy.exceptions.ErrorCode;

public class ModerationClientException extends BusinessException {
    public ModerationClientException(String message) {
        super(ErrorCode.MODERATION_CLIENT_ERROR, message);
    }
}