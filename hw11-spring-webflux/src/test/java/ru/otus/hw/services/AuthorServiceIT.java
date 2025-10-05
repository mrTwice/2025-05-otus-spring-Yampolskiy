package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.components.DataSeeder;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.data.mongodb.auto-index-creation=true",
                "spring.data.mongodb.database=library-test-${random.uuid}"
        }
)
@ActiveProfiles("test")
class AuthorServiceIT {

    @Autowired
    DataSeeder seeder;

    @Autowired
    AuthorService authorService;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    GenreRepository genreRepository;

    @Autowired
    CommentRepository commentRepository;

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
    @DisplayName("findAll: возвращает всех авторов из БД (полный контекст)")
    void findAll_returnsSeededAuthors() {
        var expected = authorRepository.findAll().collectList().block();
        assertThat(expected).isNotEmpty();

        reactor.test.StepVerifier.create(authorService.findAll().collectList())
                .assertNext(actual ->
                        assertThat(actual)
                                .hasSameSizeAs(expected)
                                .containsExactlyInAnyOrderElementsOf(expected))
                .verifyComplete();
    }
}
