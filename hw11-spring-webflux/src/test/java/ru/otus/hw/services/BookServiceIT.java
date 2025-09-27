package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.otus.hw.components.DataSeeder;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.data.mongodb.auto-index-creation=true",
                "spring.data.mongodb.database=library-test-${random.uuid}"
        }
)
@ActiveProfiles("test")
class BookServiceIT {

    @Autowired
    private DataSeeder seeder;

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    private String author1Id;
    private String author2Id;
    private String g1;
    private String g2;
    private String g3;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll()
                .then(bookRepository.deleteAll())
                .then(genreRepository.deleteAll())
                .then(authorRepository.deleteAll())
                .then(seeder.seed())
                .block();

        var authors = authorRepository.findAll().collectList().block();
        assert authors != null;
        author1Id = authors.get(0).getId();
        author2Id = authors.get(1).getId();

        var genres = genreRepository.findAll().collectList().block();
        assert genres != null;
        g1 = genres.get(0).getId();
        g2 = genres.get(1).getId();
        g3 = genres.get(2).getId();
    }

    @Test
    @DisplayName("insert: успех и нормализация жанров, поверх стартового датасета")
    void insert_happyPath_onSeededData() {
        Set<String> genres = new HashSet<>(List.of(g3, g1, g1, g2));
        Mono<Book> toInsert = bookService.insert(" New Book ", author1Id, genres);


        StepVerifier.<Book>create(toInsert)
                .assertNext(saved -> {
                    assertThat(saved.getId()).isNotBlank();
                    assertThat(saved.getTitle()).isEqualTo("New Book");
                    assertThat(saved.getAuthorId()).isEqualTo(author1Id);
                    assertThat(saved.getGenresIds())
                            .asInstanceOf(list(String.class))
                            .hasSize(3)
                            .containsExactlyInAnyOrder(g1, g2, g3);

                })
                .verifyComplete();

        var total = bookRepository.count().block();
        assertThat(total).isEqualTo(4L);
    }

    @Test
    @DisplayName("insert: дубликат {title, authorId} из сидера -> DuplicateKeyException")
    void insert_duplicateFromSeed() {
        StepVerifier.create(bookService.insert("BookTitle_1", author1Id, Set.of(g1, g2)))
                .expectErrorSatisfies(ex ->
                        assertThat(ex).isInstanceOf(DuplicateKeyException.class))
                .verify();
    }

    @Test
    @DisplayName("deleteById: удаляет книгу + только её комментарии, чужие остаются (реактивно)")
    void deleteById_withSeededComments_reactive() {
        var books = bookRepository.findAll().collectList().block();
        assert books != null;
        var k1 = books.get(0);
        var k2 = books.get(1);

        StepVerifier.create(commentRepository.findByBookIdOrderByCreatedAtDesc(k1.getId()))
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(commentRepository.findByBookIdOrderByCreatedAtDesc(k2.getId()))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(bookService.deleteById(k1.getId()))
                .verifyComplete();

        StepVerifier.create(bookRepository.findById(k1.getId()))
                .verifyComplete();

        StepVerifier.create(commentRepository.findByBookIdOrderByCreatedAtDesc(k1.getId()))
                .verifyComplete();

        StepVerifier.create(commentRepository.findByBookIdOrderByCreatedAtDesc(k2.getId()))
                .expectNextCount(1)
                .verifyComplete();
    }
}
