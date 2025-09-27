package ru.otus.hw.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;

import java.util.Set;

public interface BookService {
    Mono<Book> findById(String id);

    Flux<Book> findAll();

    Mono<Page<Book>> findAll(Pageable pageable);

    Mono<Book> insert(String title, String authorId, Set<String> genresIds);

    Mono<Book> update(String id, String title, String authorId, Set<String> genresIds, long expectedVersion);

    Mono<Void> deleteById(String id);
}
