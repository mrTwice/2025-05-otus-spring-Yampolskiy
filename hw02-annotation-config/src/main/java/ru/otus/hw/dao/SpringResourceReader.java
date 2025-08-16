package ru.otus.hw.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

@Component
public class SpringResourceReader implements ResourceReader {

    private final ResourceLoader resourceLoader;

    private final Charset charset;

    public SpringResourceReader(
            ResourceLoader resourceLoader,
            @Value("${app.io.charset:UTF-8}") String charsetName
    ) {
        this.resourceLoader = resourceLoader;
        this.charset = Charset.forName(charsetName);
    }

    @Override
    public Reader open(String location) {
        String resolved = hasPrefix(location) ? location : "classpath:" + location;
        Resource resource = resourceLoader.getResource(resolved);
        if (!resource.exists()) {
            throw new QuestionReadException("Resource not found: " + resolved);
        }
        try {
            return new InputStreamReader(resource.getInputStream(), charset);
        } catch (IOException e) {
            throw new QuestionReadException("Failed to open: " + resolved, e);
        }
    }

    private boolean hasPrefix(String s) {
        return s.startsWith("classpath:") || s.startsWith("file:") || s.startsWith("http:") || s.startsWith("https:");
    }
}
