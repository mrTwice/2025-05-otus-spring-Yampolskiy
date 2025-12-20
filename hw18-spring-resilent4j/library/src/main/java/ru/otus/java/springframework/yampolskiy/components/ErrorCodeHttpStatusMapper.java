package ru.otus.java.springframework.yampolskiy.components;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.otus.java.springframework.yampolskiy.exceptions.ErrorCode;

@Component
public class ErrorCodeHttpStatusMapper {
    public HttpStatus toStatus(ErrorCode code) {
        return switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION, MODERATION_CLIENT_ERROR -> HttpStatus.BAD_REQUEST;

            case ASSOCIATION_VIOLATION, COMMENT_FORBIDDEN -> HttpStatus.UNPROCESSABLE_ENTITY;

            case CONFLICT, DUPLICATE, DELETION_NOT_ALLOWED -> HttpStatus.CONFLICT;

            case MODERATION_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
        };
    }
}
