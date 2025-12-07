package ru.otus.hw.components;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.BookRepository;

@Component
@RequiredArgsConstructor
public class LibraryMetrics {

    private final BookRepository bookRepository;

    private final MeterRegistry registry;

    @PostConstruct
    public void init() {
        Gauge.builder("library.books.total", bookRepository, BookRepository::count)
                .description("Current number of books in DB")
                .register(registry);
    }
}
