package ru.otus.java.springframework.yampolskiy.integretion.dto;

import java.time.Instant;

public record ApiError(
        String code,
        String message,
        Instant timestamp,
        String path
) {}