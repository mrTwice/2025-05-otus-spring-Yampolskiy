package ru.otus.hw.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.otus.hw.components.DataSeeder;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Set;

@DataMongoTest(properties = {
        "spring.data.mongodb.auto-index-creation=true",
        "spring.data.mongodb.database=library-test-${random.uuid}"
})
@ActiveProfiles("test")
@Import(DataSeeder.class)
class GenreRepositoryIT {

    @Autowired private GenreRepository genreRepository;

    @Autowired private AuthorRepository authorRepository;

    @Autowired private BookRepository bookRepository;

    @Autowired private CommentRepository commentRepository;

    @Autowired private DataSeeder seeder;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll()
                .then(bookRepository.deleteAll())
                .then(genreRepository.deleteAll())
                .then(authorRepository.deleteAll())
                .then(seeder.seed())
                .block();
    }

    @Test
    @DisplayName("findByName: находит жанр по точному имени (сид-данные)")
    void findByName_found() {
        StepVerifier.create(genreRepository.findByName("Genre_1"))
                .expectNextMatches(g -> g.getName().equals("Genre_1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findByName: пусто для несуществующего имени")
    void findByName_missing() {
        StepVerifier.create(genreRepository.findByName("No Such Genre"))
                .verifyComplete();
    }

    @Test
    @DisplayName("existsByName: true для существующего и false для отсутствующего")
    void existsByName_trueFalse() {
        StepVerifier.create(genreRepository.existsByName("Genre_2"))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(genreRepository.existsByName("Unknown"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("findByIdIn: возвращает только найденные id")
    void findByIdIn_returnsOnlyExisting() {
        StepVerifier.create(
                        genreRepository.findAll().take(2).collectList()
                                .flatMapMany(list -> {
                                    org.assertj.core.api.Assertions.assertThat(list).hasSizeGreaterThanOrEqualTo(2);
                                    String g1 = list.get(0).getId();
                                    String g2 = list.get(1).getId();
                                    return genreRepository.findByIdIn(Set.of(g1, g2, "missing-id"));
                                })
                                .map(Genre::getId)
                                .collectList()
                )
                .assertNext(ids -> org.assertj.core.api.Assertions.assertThat(ids).hasSize(2))
                .verifyComplete();
    }


    @Test
    @DisplayName("unique(name): попытка сохранить дубликат имени бросает DuplicateKeyException")
    void unique_name_duplicate() {
        StepVerifier.create(genreRepository.save(new Genre(null, "Genre_1", 0L)))
                .expectError(DuplicateKeyException.class)
                .verify();
    }
}
