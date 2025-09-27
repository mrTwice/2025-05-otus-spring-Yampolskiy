package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;

public interface AuthorRepository extends ReactiveMongoRepository<Author, String> {
    Mono<Boolean> existsByFullName(String fullName);

    Mono<Author> findByFullName(String fullName);
}
