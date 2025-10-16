package ru.otus.hw.migration.pg2mongo;

public class MissingMappingException extends RuntimeException {
    public MissingMappingException(String message) {
        super(message);
    }
}
