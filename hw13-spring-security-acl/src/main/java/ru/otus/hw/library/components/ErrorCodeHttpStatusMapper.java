package ru.otus.hw.library.components;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.otus.hw.exceptions.ErrorCode;

@Component
public class ErrorCodeHttpStatusMapper {
    public HttpStatus toStatus(ErrorCode code) {
        return switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case ASSOCIATION_VIOLATION -> HttpStatus.UNPROCESSABLE_ENTITY;
            case CONFLICT, DUPLICATE, DELETION_NOT_ALLOWED -> HttpStatus.CONFLICT;
        };
    }
}