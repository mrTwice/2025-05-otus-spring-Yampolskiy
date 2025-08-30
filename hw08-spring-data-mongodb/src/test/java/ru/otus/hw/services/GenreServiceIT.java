package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.config.DataSeeder;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class GenreServiceIT {

    @Autowired
    private GenreService genreService;

    @Autowired
    private DataSeeder seeder;

    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        seeder.seed();
    }

    @Test
    @DisplayName("findAll: возвращает все жанры из БД (соответствуют данным сидера)")
    void findAll_returnsSeededGenres() {
        List<Genre> fromService = genreService.findAll();
        List<Genre> inDb = genreRepository.findAll();

        assertThat(fromService)
                .isNotNull()
                .isNotEmpty()
                .hasSameSizeAs(inDb);

        assertThat(fromService).containsExactlyInAnyOrderElementsOf(inDb);
    }
}
