package ru.otus.hw.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.GenreRepository;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(
        properties = {
                "spring.shell.interactive.enabled=false",
                "spring.shell.script.enabled=false"
        }
)
class BookServiceIT {

    @Autowired
    BookService bookService;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    GenreRepository genreRepository;

    @Test
    void loadedBook_hasAccessibleAuthorAndGenres_outsideServiceTx() {
        var authorId = authorRepository.findAll().get(0).getId();

        var allGenres = genreRepository.findAll();
        assertThat(allGenres)
                .as("В тестовых данных должно быть минимум 2 жанра")
                .hasSizeGreaterThanOrEqualTo(2);

        var genreIds = allGenres.stream()
                .map(Genre::getId)
                .limit(2)
                .collect(java.util.stream.Collectors.toSet());

        var saved = bookService.insert("IT-Book", authorId, genreIds);

        var loaded = bookService.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getAuthor().getId()).isEqualTo(authorId);
        assertThat(loaded.getGenres())
                .extracting(Genre::getId)
                .containsAll(genreIds);
    }
}
