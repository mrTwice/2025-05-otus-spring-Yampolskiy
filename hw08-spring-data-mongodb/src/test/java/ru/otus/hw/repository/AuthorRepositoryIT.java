package ru.otus.hw.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.config.DataSeeder;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@ActiveProfiles("test")
@Import(DataSeeder.class)
class AuthorRepositoryIT {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    DataSeeder seeder;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        seeder.seed();
    }

    @Test
    @DisplayName("findByFullName: находит автора по точному имени (сид-данные)")
    void findByFullName_found() {
        assertThat(authorRepository.findByFullName("Author_1"))
                .isPresent()
                .get()
                .extracting(Author::getFullName)
                .isEqualTo("Author_1");
    }

    @Test
    @DisplayName("existsByFullName: true для существующего и false для отсутствующего")
    void existsByFullName_trueFalse() {
        assertThat(authorRepository.existsByFullName("Author_2")).isTrue();
        assertThat(authorRepository.existsByFullName("No Such Author")).isFalse();
    }

    @Test
    @DisplayName("unique(fullName): при сохранении дубликата бросает DuplicateKeyException")
    void unique_fullName_duplicate() {
        // в сидере уже есть Author_3
        assertThatThrownBy(() -> authorRepository.save(new Author(null, "Author_3")))
                .isInstanceOf(DuplicateKeyException.class);
    }
}

