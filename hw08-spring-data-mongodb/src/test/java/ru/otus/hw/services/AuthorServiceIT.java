package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.config.DataSeeder;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class AuthorServiceIT {

    @Autowired
    DataSeeder seeder;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        seeder.seed();
    }

    @Test
    @DisplayName("findAll: возвращает всех авторов из БД (полный контекст)")
    void findAll_returnsSeededAuthors() {
        List<Author> expected = authorRepository.findAll();
        assertThat(expected).isNotEmpty();

        List<Author> actual = authorService.findAll();

        assertThat(actual)
                .hasSameSizeAs(expected)
                .containsExactlyInAnyOrderElementsOf(expected);
    }
}