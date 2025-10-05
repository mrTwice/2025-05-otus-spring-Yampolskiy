package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Genre;

import java.util.Set;

public interface GenreRepository extends ReactiveMongoRepository<Genre, String> {
    Flux<Genre> findByIdIn(Set<String> ids);

    Mono<Genre> findByName(String name);

    Mono<Boolean> existsByName(String name);
}
