package ru.otus.hw.components;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookMetrics {

    private final MeterRegistry registry;

    private Counter booksCreated;

    private Counter booksDeleted;

    @PostConstruct
    public void init() {
        booksCreated = Counter.builder("library.books.created")
                .description("Number of created books")
                .register(registry);

        booksDeleted = Counter.builder("library.books.deleted")
                .description("Number of books was deleted from library")
                .register(registry);

    }

    public void incrementCreated() {
        booksCreated.increment();
    }

    public void incrementDeleted() {
        booksDeleted.increment();
    }
}
