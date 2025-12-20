package ru.otus.java.springframework.yampolskiy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "moderation")
public class ModerationProperties {

    private final Failure failure = new Failure();

    private final Rules rules = new Rules();

    @Setter
    @Getter
    public static class Failure {

        private double errorProbability;

        private double slowProbability;

        private long slowDelayMs;

    }

    @Setter
    @Getter
    public static class Rules {

        private List<String> forbidden = new ArrayList<>();

    }
}
