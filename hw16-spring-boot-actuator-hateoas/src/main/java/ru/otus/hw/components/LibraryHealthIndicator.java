package ru.otus.hw.components;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@Component
@RequiredArgsConstructor
public class LibraryHealthIndicator implements HealthIndicator {

    private final BookRepository bookRepository;

    private final GenreRepository genreRepository;

    @Override
    public Health health() {
        try {
            long books = bookRepository.count();
            long genres = genreRepository.count();

            if (genres == 0) {
                return Health.status("DEGRADED")
                        .withDetail("reason", "No genres in DB")
                        .withDetail("booksCount", books)
                        .withDetail("genresCount", genres)
                        .build();
            }

            return Health.up()
                    .withDetail("booksCount", books)
                    .withDetail("genresCount", genres)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("reason", "DB check failed")
                    .build();
        }
    }
}
