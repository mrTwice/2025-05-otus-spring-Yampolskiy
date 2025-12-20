package ru.otus.java.springframework.yampolskiy.integretion.dto;

public record ModerationResponse(boolean allowed, String reason) {

    public static ModerationResponse ok() {
        return new ModerationResponse(true, null);
    }

    public static ModerationResponse rejected(String reason) {
        return new ModerationResponse(false, reason);
    }

}

