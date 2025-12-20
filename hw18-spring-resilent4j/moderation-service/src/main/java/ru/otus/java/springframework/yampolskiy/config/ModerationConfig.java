package ru.otus.java.springframework.yampolskiy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
@EnableConfigurationProperties(ModerationProperties.class)
public class ModerationConfig {

    @Bean
    public Random moderationRandom() {
        return new Random();
    }
}
