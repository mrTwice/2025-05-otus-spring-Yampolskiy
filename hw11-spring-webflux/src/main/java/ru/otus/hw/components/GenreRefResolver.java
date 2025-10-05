package ru.otus.hw.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GenreRefResolver {
    private final GenreRepository genreRepository;

    public Flux<Genre> byIds(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }
        return genreRepository.findByIdIn(ids);
    }
}
