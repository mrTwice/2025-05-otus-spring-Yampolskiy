package ru.otus.hw.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.otus.hw.components.DataSeeder;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest(properties = {
        "spring.data.mongodb.auto-index-creation=true",
        "spring.data.mongodb.database=library-test-${random.uuid}"
})
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
        commentRepository.deleteAll()
                .then(bookRepository.deleteAll())
                .then(genreRepository.deleteAll())
                .then(authorRepository.deleteAll())
                .then(seeder.seed())
                .block();
    }

    @Test
    @DisplayName("findByFullName: находит автора по точному имени (сид-данные)")
    void findByFullName_found() {
        StepVerifier.create(authorRepository.findByFullName("Author_1"))
                .assertNext(a -> assertThat(a.getFullName()).isEqualTo("Author_1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("existsByFullName: true для существующего и false для отсутствующего")
    void existsByFullName_trueFalse() {
        StepVerifier.create(authorRepository.existsByFullName("Author_2"))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(authorRepository.existsByFullName("No Such Author"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("unique(fullName): при сохранении дубликата бросает DuplicateKeyException")
    void unique_fullName_duplicate() {
        StepVerifier.create(authorRepository.save(new Author(null, "Author_3", 0L)))
                .expectError(DuplicateKeyException.class)
                .verify();
    }
}