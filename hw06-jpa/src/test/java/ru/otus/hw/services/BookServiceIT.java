package ru.otus.hw.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.GenreRepository;

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
        var genreIds = genreRepository.findAll().subList(0, 2).stream()
                .map(Genre::getId).collect(java.util.stream.Collectors.toSet());

        var saved = bookService.insert("IT-Book", authorId, genreIds);

        var loaded = bookService.findById(saved.getId()).orElseThrow();

        org.assertj.core.api.Assertions.assertThat(loaded.getAuthor().getId()).isEqualTo(authorId);
        org.assertj.core.api.Assertions.assertThat(loaded.getGenres())
                .extracting(Genre::getId)
                .containsAll(genreIds);
    }
}
