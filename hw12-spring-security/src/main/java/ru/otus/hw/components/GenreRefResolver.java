package ru.otus.hw.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenreRefResolver {
    private final GenreRepository genreRepository;

    public Set<Genre> byIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return ids.stream()
                .map(id -> genreRepository.findById(id).orElse(null))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}