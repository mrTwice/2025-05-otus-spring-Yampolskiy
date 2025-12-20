package ru.otus.java.springframework.yampolskiy.component;

import ru.otus.java.springframework.yampolskiy.dto.ModerationMode;

public final class ModerationModeResolver {

    private ModerationModeResolver() {

    }

    public static ModerationMode resolve(String headerMode, String queryMode) {
        String raw = (headerMode != null && !headerMode.isBlank()) ? headerMode : queryMode;
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ModerationMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown X-Mode/mode: " + raw);
        }
    }
}
