package ru.otus.hw.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MongoDBInitializer {

    @Bean
    @Profile("!test")
    public CommandLineRunner seed(DataSeeder seeder) {
        return args -> seeder.seed();
    }
}

