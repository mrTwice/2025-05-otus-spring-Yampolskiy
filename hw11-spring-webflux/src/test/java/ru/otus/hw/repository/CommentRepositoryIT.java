package ru.otus.hw.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.otus.hw.components.DataSeeder;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest(properties = {
        "spring.data.mongodb.auto-index-creation=true",
        "spring.data.mongodb.database=library-test-${random.uuid}"
})
@ActiveProfiles("test")
@Import(DataSeeder.class)
class CommentRepositoryIT {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private DataSeeder seeder;

    private String book1Id;

    private String book2Id;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll()
                .then(bookRepository.deleteAll())
                .then(genreRepository.deleteAll())
                .then(authorRepository.deleteAll())
                .then(seeder.seed())
                .block();

        book1Id = bookRepository.findAll()
                .filter(b -> "BookTitle_1".equals(b.getTitle()))
                .next()
                .map(Book::getId)
                .block();

        book2Id = bookRepository.findAll()
                .filter(b -> "BookTitle_2".equals(b.getTitle()))
                .next()
                .map(Book::getId)
                .block();
    }

    @Test
    @DisplayName("findByBookIdOrderByCreatedAtDesc: сортирует по createdAt по убыванию (первые три)")
    void findByBookId_sorted_desc() {
        Instant now = Instant.now();

        Flux<Comment> saved = Flux.just(
                new Comment(null, "t1", now.minusSeconds(3), book1Id, 0L),
                new Comment(null, "t2", now.minusSeconds(2), book1Id, 0L),
                new Comment(null, "t3", now.minusSeconds(1), book1Id, 0L)
        ).flatMap(commentRepository::save);

        StepVerifier.create(
                        saved.thenMany(
                                commentRepository.findByBookIdOrderByCreatedAtDesc(book1Id)
                                        .map(Comment::getText)
                                        .take(3)
                                        .collectList()
                        )
                )
                .assertNext(list -> {
                    assertThat(list).containsExactly("t3", "t2", "t1");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findByBookIdOrderByCreatedAtDesc: для неизвестной книги возвращает пусто")
    void findByBookId_unknown_returnsEmpty() {
        StepVerifier.create(
                        commentRepository.findByBookIdOrderByCreatedAtDesc("missing").collectList()
                )
                .assertNext(list -> assertThat(list).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteByBookId: удаляет только комментарии указанной книги и возвращает их количество")
    void deleteByBookId_deletesOnlyTargetBooksComments() {
        Instant now = Instant.now();

        Mono.when(
                commentRepository.save(new Comment(null, "a1", now.minusSeconds(3), book1Id, 0L)),
                commentRepository.save(new Comment(null, "a2", now.minusSeconds(2), book1Id, 0L)),
                commentRepository.save(new Comment(null, "b1", now.minusSeconds(1), book2Id, 0L))
        ).block();

        long beforeBook1 = commentRepository.findByBookIdOrderByCreatedAtDesc(book1Id)
                .count()
                .blockOptional()
                .orElse(0L);

        long beforeBook2 = commentRepository.findByBookIdOrderByCreatedAtDesc(book2Id)
                .count()
                .blockOptional()
                .orElse(0L);

        long deleted = commentRepository.deleteByBookId(book1Id)
                .blockOptional()
                .orElse(0L);

        List<Comment> leftBook1 = commentRepository.findByBookIdOrderByCreatedAtDesc(book1Id)
                .collectList()
                .blockOptional()
                .orElseGet(List::of);

        List<Comment> leftBook2 = commentRepository.findByBookIdOrderByCreatedAtDesc(book2Id)
                .collectList()
                .blockOptional()
                .orElseGet(List::of);

        assertThat(deleted).isEqualTo(beforeBook1);
        assertThat(leftBook1).isEmpty();
        assertThat(leftBook2).hasSize((int) beforeBook2);
    }
}
