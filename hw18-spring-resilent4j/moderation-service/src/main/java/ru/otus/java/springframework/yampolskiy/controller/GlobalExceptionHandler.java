package ru.otus.java.springframework.yampolskiy.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.java.springframework.yampolskiy.dto.ApiError;
import ru.otus.java.springframework.yampolskiy.exception.ModerationInternalException;
import ru.otus.java.springframework.yampolskiy.exception.ModerationUnavailableException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiError(
                        ex.getStatusCode().toString(),
                        ex.getReason(),
                        Instant.now(),
                        req.getRequestURI()
                ));
    }

    @ExceptionHandler(ModerationUnavailableException.class)
    public ResponseEntity<ApiError> handleUnavailable(
            ModerationUnavailableException ex,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("MODERATION_UNAVAILABLE", ex.getMessage(), Instant.now(), req.getRequestURI()));
    }

    @ExceptionHandler(ModerationInternalException.class)
    public ResponseEntity<ApiError> handleInternal(
            ModerationInternalException ex,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("MODERATION_INTERNAL_ERROR", ex.getMessage(), Instant.now(), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(
            Exception ex,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("UNEXPECTED_ERROR", "Unexpected error", Instant.now(), req.getRequestURI()));
    }
}

