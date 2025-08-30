package ru.otus.hw.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.config.DataSeeder;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@ActiveProfiles("test")
@EnableMongoRepositories(basePackageClasses = GenreRepository.class)
@Import(DataSeeder.class)
class GenreRepositoryIT {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DataSeeder seeder;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        seeder.seed();
    }

    @Test
    @DisplayName("findByName: находит жанр по точному имени (сид-данные)")
    void findByName_found() {
        assertThat(genreRepository.findByName("Genre_1"))
                .isPresent()
                .get()
                .extracting(Genre::getName)
                .isEqualTo("Genre_1");
    }

    @Test
    @DisplayName("findByName: пусто для несуществующего имени")
    void findByName_missing() {
        assertThat(genreRepository.findByName("No Such Genre")).isNotPresent();
    }

    @Test
    @DisplayName("existsByName: true для существующего и false для отсутствующего")
    void existsByName_trueFalse() {
        assertThat(genreRepository.existsByName("Genre_2")).isTrue();
        assertThat(genreRepository.existsByName("Unknown")).isFalse();
    }

    @Test
    @DisplayName("findByIdIn: возвращает только найденные id")
    void findByIdIn_returnsOnlyExisting() {
        var all = genreRepository.findAll();
        var g1 = all.get(0).getId();
        var g2 = all.get(1).getId();

        var result = genreRepository.findByIdIn(List.of(g1, g2, "missing-id"));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Genre::getId).containsExactlyInAnyOrder(g1, g2);
    }

    @Test
    @DisplayName("unique(name): попытка сохранить дубликат имени бросает DuplicateKeyException")
    void unique_name_duplicate() {
        assertThatThrownBy(() -> genreRepository.save(new Genre(null, "Genre_1")))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
