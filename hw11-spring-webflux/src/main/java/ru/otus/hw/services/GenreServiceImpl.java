package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    @Override
    public Flux<Genre> findAll() {
        return genreRepository.findAll(Sort.by("name").ascending());
    }

    @Override
    public Mono<Page<Genre>> findAll(Pageable pageable) {
        Mono<Long> total = genreRepository.count();
        Mono<List<Genre>> content = genreRepository.findAll(pageable.getSort())
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .collectList();

        return Mono.zip(content, total)
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    @Override
    public Mono<Genre> insert(String name) {
        return Mono.defer(() -> {
            String normalized = normalize(name);
            return genreRepository.save(new Genre(null, normalized, 0L));
        });
    }

    @Override
    public Mono<List<Genre>> findByIds(Set<String> ids) {
        return genreRepository.findByIdIn(ids).collectList();
    }

    private String normalize(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new ValidationException("Genre name must not be blank");
        }
        return s.trim();
    }
}
