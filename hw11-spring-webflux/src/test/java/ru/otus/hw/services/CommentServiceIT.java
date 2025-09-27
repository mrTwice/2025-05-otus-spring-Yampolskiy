package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple4;
import ru.otus.hw.components.DataSeeder;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CommentServiceIT {

    @Autowired
    private CommentService commentService;

    @Autowired
    private DataSeeder dataSeeder;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private String book1Id;
    private String book2Id;

    @BeforeEach
    void setUp() {
        Mono.when(
                commentRepository.deleteAll(),
                bookRepository.deleteAll(),
                genreRepository.deleteAll(),
                authorRepository.deleteAll()
        ).then(dataSeeder.seed()).block();

        var books = bookRepository.findAll()
                .collectSortedList(Comparator.comparing(Book::getTitle))
                .block();
        assert books != null;
        book1Id = books.stream().filter(b -> "BookTitle_1".equals(b.getTitle())).findFirst().orElseThrow().getId();
        book2Id = books.stream().filter(b -> "BookTitle_2".equals(b.getTitle())).findFirst().orElseThrow().getId();
    }

    @Test
    @DisplayName("findById: возвращает комментарий, если он существует")
    void findById_exists() {
        var toSave = new Comment(null, "hello", null, book1Id, 0L);

        StepVerifier.create(
                        commentRepository.save(toSave)
                                .flatMap(saved -> commentService.findById(saved.getId()))
                )
                .assertNext(found -> {
                    assertThat(found.getText()).isEqualTo("hello");
                    assertThat(found.getBookId()).isEqualTo(book1Id);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findById: пустой результат для несуществующего id → NotFoundException")
    void findById_missing() {
        StepVerifier.create(commentService.findById("missing"))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NotFoundException.class);
                    assertThat(e.getMessage()).contains("Comment with id missing not found");
                })
                .verify();
    }


    @Test
    @DisplayName("findByBookId: bookId пустой/blank → ValidationException")
    void findByBookId_blank() {
        StepVerifier.create(commentService.findByBookId("  "))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ValidationException.class);
                    assertThat(e.getMessage()).contains("Book id must not be null or blank");
                })
                .verify();

        StepVerifier.create(commentService.findByBookId(null))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    @DisplayName("findByBookId: книга не существует → EntityNotFoundException")
    void findByBookId_bookNotFound() {
        StepVerifier.create(commentService.findByBookId("missing"))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NotFoundException.class);
                    assertThat(e.getMessage()).contains("Book with id missing not found");
                })
                .verify();
    }

    @Test
    @DisplayName("insert: happy-path — тримит текст, проставляет createdAt, сохраняет")
    void insert_happyPath() {
        StepVerifier.create(commentService.insert(book1Id, "  New comment  "))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isNotBlank();
                    assertThat(saved.getText()).isEqualTo("New comment");
                    assertThat(saved.getBookId()).isEqualTo(book1Id);
                    assertThat(saved.getCreatedAt()).isNotNull();
                })
                .verifyComplete();

        StepVerifier.create(
                        commentRepository.findByBookIdOrderByCreatedAtDesc(book1Id).next()
                )
                .assertNext(inDb -> {
                    assertThat(inDb.getText()).isEqualTo("New comment");
                    assertThat(inDb.getBookId()).isEqualTo(book1Id);

                    assertThat(inDb.getCreatedAt())
                            .isBetween(Instant.now().minus(5, ChronoUnit.SECONDS), Instant.now().plus(5, ChronoUnit.SECONDS));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("insert: text пустой/blank → ValidationException")
    void insert_blankText() {
        StepVerifier.create(commentService.insert(book1Id, "   "))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ValidationException.class);
                    assertThat(e.getMessage()).contains("Comment text must not be blank");
                })
                .verify();

        StepVerifier.create(commentService.insert(book1Id, null))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ValidationException.class);
                    assertThat(e.getMessage()).contains("Comment text must not be blank");
                })
                .verify();
    }

    @Test
    @DisplayName("insert: bookId пустой/blank → ValidationException")
    void insert_blankBookId() {
        StepVerifier.create(commentService.insert("  ", "text"))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ValidationException.class);
                    assertThat(e.getMessage()).contains("Book id must not be null or blank");
                })
                .verify();

        StepVerifier.create(commentService.insert(null, "text"))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    @DisplayName("insert: книга не существует → EntityNotFoundException")
    void insert_bookNotFound() {
        StepVerifier.create(commentService.insert("missing", "text"))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NotFoundException.class);
                    assertThat(e.getMessage()).contains("Book with id missing not found");
                })
                .verify();
    }

    @Test
    @DisplayName("update: text пустой/blank → ValidationException")
    void update_blankText() {
        var seed = new Comment(null, "x", null, book1Id, 0L);

        StepVerifier.create(
                        commentRepository.save(seed)
                                .flatMap(c -> commentService.update(c.getId(), "   "))
                )
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ValidationException.class);
                    assertThat(e.getMessage()).contains("Comment text must not be blank");
                })
                .verify();

        StepVerifier.create(
                        commentRepository.save(seed)
                                .flatMap(c -> commentService.update(c.getId(), null))
                )
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ValidationException.class);
                    assertThat(e.getMessage()).contains("Comment text must not be blank");
                })
                .verify();
    }


    @Test
    @DisplayName("update: комментарий не найден → EntityNotFoundException")
    void update_notFound() {
        StepVerifier.create(commentService.update("missing", "new"))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NotFoundException.class);
                    assertThat(e.getMessage()).contains("Comment with id missing not found");
                })
                .verify();
    }

    @Test
    @DisplayName("deleteById: удаляет существующий комментарий, остальные остаются")
    void deleteById_deletesOnlyTarget() {
        Instant now = Instant.now();

        var c1 = new Comment(null, "c1", now.minusSeconds(3), book1Id, 0L);
        var c2 = new Comment(null, "c2", now.minusSeconds(2), book1Id, 0L);
        var cOther = new Comment(null, "other", now.minusSeconds(1), book2Id, 0L);

        Mono<Tuple4<Long, Long, Long, Long>> scenario =
                Mono.zip(
                                commentRepository.countByBookId(book1Id),
                                commentRepository.countByBookId(book2Id)
                        )
                        .flatMap(bBefore ->
                                commentRepository.saveAll(java.util.List.of(c1, c2, cOther))
                                        .then(Mono.just(bBefore))
                        )
                        .flatMap(bBefore ->
                                commentRepository.findByBookIdOrderByCreatedAtDesc(book1Id)
                                        .filter(c -> "c1".equals(c.getText()))
                                        .next()
                                        .map(Comment::getId)
                                        .flatMap(id -> commentService.deleteById(id).thenReturn(bBefore))
                        )
                        .flatMap(bBefore ->
                                Mono.zip(
                                        Mono.just(bBefore.getT1()),
                                        Mono.just(bBefore.getT2()),
                                        commentRepository.countByBookId(book1Id),
                                        commentRepository.countByBookId(book2Id)
                                )
                        );

        StepVerifier.create(scenario)
                .assertNext(t -> {
                    long b1 = t.getT1();
                    long b2 = t.getT2();
                    long after1 = t.getT3();
                    long after2 = t.getT4();

                    assertThat(after1).as("book1 total after delete").isEqualTo(b1 + 1);
                    assertThat(after2).as("book2 total after insert").isEqualTo(b2 + 1);
                })
                .verifyComplete();

        StepVerifier.create(
                        commentRepository.findByBookIdOrderByCreatedAtDesc(book1Id)
                                .filter(c -> "c2".equals(c.getText()))
                                .take(1)
                ).expectNextMatches(c -> "c2".equals(c.getText()))
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteById: несуществующий id → no-op")
    void deleteById_missing_noop() {
        var seed = new Comment(null, "x", null, book1Id, 0L);

        StepVerifier.create(commentRepository.save(seed))
                .assertNext(saved -> assertThat(saved.getId()).isNotBlank())
                .verifyComplete();

        var beforeCount = commentRepository.count();

        StepVerifier.create(commentService.deleteById("missing"))
                .verifyComplete();

        StepVerifier.create(
                        Mono.zip(beforeCount, commentRepository.count())
                )
                .assertNext(t -> assertThat(t.getT2()).isEqualTo(t.getT1()))
                .verifyComplete();
    }
}