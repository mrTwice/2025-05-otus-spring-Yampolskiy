package ru.otus.hw.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final AuthorRepository authors;

    private final GenreRepository genres;

    private final BookRepository books;

    private final CommentRepository comments;


    public Mono<Void> seed() {
        return authors.count()
                .flatMap(cnt -> cnt > 0 ? Mono.<Void>empty() : seedAll())
                .doOnSubscribe(s -> System.out.println("seed() start"))
                .doOnSuccess(v -> System.out.println("seed() complete"));
    }

    private Mono<Void> seedAll() {
        return seedAuthors().collectList()
                .zipWith(seedGenres().collectList())
                .flatMap(tuple -> seedBooks(tuple.getT1(), tuple.getT2()).collectList())
                .flatMap(this::seedComments)
                .timeout(Duration.ofSeconds(10))
                .doOnError(e -> e.printStackTrace())
                .then();
    }


    private Flux<Author> seedAuthors() {
        return authors.saveAll(Flux.just(
                new Author(null, "Author_1", 0L),
                new Author(null, "Author_2", 0L),
                new Author(null, "Author_3", 0L)
        ));
    }

    private Flux<Genre> seedGenres() {
        return genres.saveAll(Flux.just(
                new Genre(null, "Genre_1", 0L),
                new Genre(null, "Genre_2", 0L),
                new Genre(null, "Genre_3", 0L),
                new Genre(null, "Genre_4", 0L),
                new Genre(null, "Genre_5", 0L),
                new Genre(null, "Genre_6", 0L)
        ));
    }

    private Flux<Book> seedBooks(List<Author> savedAuthors, List<Genre> savedGenres) {
        var g1 = savedGenres.get(0);
        var g2 = savedGenres.get(1);
        var g3 = savedGenres.get(2);
        var g4 = savedGenres.get(3);
        var g5 = savedGenres.get(4);
        var g6 = savedGenres.get(5);

        return books.saveAll(Flux.just(
                new Book(null, "BookTitle_1", savedAuthors.get(0).getId(),
                        List.of(g1.getId(), g2.getId()), 0L),
                new Book(null, "BookTitle_2", savedAuthors.get(1).getId(),
                        List.of(g3.getId(), g4.getId()), 0L),
                new Book(null, "BookTitle_3", savedAuthors.get(2).getId(),
                        List.of(g5.getId(), g6.getId()), 0L)
        ));
    }

    private Mono<Void> seedComments(List<Book> savedBooks) {
        return comments.saveAll(Flux.just(
                new Comment(null, "Great book!", null, savedBooks.get(0).getId(), 0L),
                new Comment(null, "Not my cup of tea", null, savedBooks.get(0).getId(), 0L),
                new Comment(null, "Awesome read", null, savedBooks.get(1).getId(), 0L)
        )).then();
    }
}
