package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findByBookIdOrderByCreatedAtDesc(String bookId);

    Mono<Long> countByBookId(String bookId);

    Mono<Long> deleteByBookId(String bookId);
}