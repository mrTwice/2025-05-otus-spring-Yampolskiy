package ru.otus.hw.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

public interface GenreService {
    Flux<Genre> findAll();

    Mono<Page<Genre>> findAll(Pageable p);

    Mono<Genre> insert(String name);

    Mono<List<Genre>> findByIds(Set<String> ids);
}