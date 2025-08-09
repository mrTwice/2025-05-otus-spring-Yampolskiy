package ru.otus.hw.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.otus.hw.config.TestConfig;
import ru.otus.hw.config.TestFileNameProvider;

@Data
@Component
public class AppProperties implements TestConfig, TestFileNameProvider {

    private final int rightAnswersCountToPass;

    private final String testFileName;

    public AppProperties(
            @Value("${test.rightAnswersCountToPass}") int rightAnswersCountToPass,
            @Value("${test.fileName}") String testFileName
    ) {
        this.rightAnswersCountToPass = rightAnswersCountToPass;
        this.testFileName = testFileName;
    }
}
