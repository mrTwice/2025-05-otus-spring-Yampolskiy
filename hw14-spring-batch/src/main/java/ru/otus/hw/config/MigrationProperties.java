package ru.otus.hw.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

    private int chunkSize = 500;

    private int splitThreads = 2;

    private boolean truncateBeforeLoad = false;

    private int skipLimit = 100;

    private String since;
}
