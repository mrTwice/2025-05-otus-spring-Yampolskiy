package ru.otus.hw.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.library.models.Book;
import ru.otus.hw.library.repositories.AuthorRepository;
import ru.otus.hw.library.repositories.GenreRepository;
import ru.otus.hw.library.services.BookService;
import ru.otus.hw.library.services.CommentService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.shell.interactive.enabled=false",
        "spring.shell.script.enabled=false"
})
class CommentServiceIT {

    @Autowired
    BookService bookService;

    @Autowired
    CommentService commentService;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    GenreRepository genreRepository;

    @Test
    void commentsByBook_haveAccessibleBookOutsideServiceTx() {
        long authorId = authorRepository.findAll().get(0).getId();
        Long g1 = genreRepository.findAll().get(0).getId();

        Book b = bookService.insert("B", authorId, Set.of(g1));

        var c1 = commentService.insert(b.getId(), authorId, "c1");
        var c2 = commentService.insert(b.getId(), authorId, "c2");

        var list = commentService.findByBookId(b.getId());

        assertThat(list).extracting(c -> c.getBook().getId()).containsOnly(b.getId());
        assertThat(list.get(0).getText()).isEqualTo("c2");
        assertThat(list.get(1).getText()).isEqualTo("c1");
    }
}