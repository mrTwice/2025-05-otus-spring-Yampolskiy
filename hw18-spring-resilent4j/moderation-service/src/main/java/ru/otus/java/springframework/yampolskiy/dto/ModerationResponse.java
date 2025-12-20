package ru.otus.java.springframework.yampolskiy.dto;

public record ModerationResponse(boolean allowed, String reason) {}