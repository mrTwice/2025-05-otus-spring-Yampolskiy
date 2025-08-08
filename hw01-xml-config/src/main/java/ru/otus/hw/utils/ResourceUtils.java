package ru.otus.hw.utils;

import ru.otus.hw.exceptions.QuestionReadException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class ResourceUtils {

    private ResourceUtils() {
    }

    public static Reader getReaderFromResource(String fileName) {
        InputStream inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new QuestionReadException("Resource not found: " + fileName);
        }
        return new InputStreamReader(inputStream);
    }
}