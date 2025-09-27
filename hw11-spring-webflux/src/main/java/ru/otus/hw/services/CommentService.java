package ru.otus.hw.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

public interface CommentService {
    Mono<Comment> findById(String id);

    Flux<Comment> findByBookId(String bookId);

    Mono<Page<Comment>> findByBookId(String bookId, Pageable page);

    Mono<Comment> insert(String bookId, String text);

    Mono<Comment> update(String id, String text);

    Mono<Void> deleteById(String id);
}
