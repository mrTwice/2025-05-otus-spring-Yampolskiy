package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.config.DataSeeder;
import ru.otus.hw.exceptions.ConflictException;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class BookServiceIT {

    @Autowired
    private DataSeeder seeder;

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    private String author1Id;
    private String author2Id;
    private String g1;
    private String g2;
    private String g3;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        seeder.seed();

        var authors = authorRepository.findAll();
        author1Id = authors.get(0).getId();
        author2Id = authors.get(1).getId();

        var genres = genreRepository.findAll();
        g1 = genres.get(0).getId();
        g2 = genres.get(1).getId();
        g3 = genres.get(2).getId();
    }

    @Test
    @DisplayName("insert: успех и нормализация жанров, поверх стартового датасета")
    void insert_happyPath_onSeededData() {
        var saved = bookService.insert(" New Book ", author1Id, new HashSet<>(List.of(g3, g1, g1, g2)));

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getTitle()).isEqualTo("New Book");
        assertThat(saved.getAuthorId()).isEqualTo(author1Id);
        assertThat(saved.getGenreIds()).containsExactly(g1, g2, g3);

        assertThat(bookRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("insert: дубликат {title, authorId} из сидера -> ConflictException")
    void insert_duplicateFromSeed() {
        assertThatThrownBy(() ->
                bookService.insert("BookTitle_1", author1Id, Set.of(g1, g2)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Книга с таким названием для этого автора уже существует");
    }

    @Test
    @DisplayName("deleteById: удаляет книгу + только её комментарии, чужие остаются")
    void deleteById_withSeededComments() {
        var books = bookRepository.findAll();
        var k1 = books.get(0);
        var k2 = books.get(1);

        var beforeK1 = commentRepository.findByBookIdOrderByCreatedAtDesc(k1.getId());
        var beforeK2 = commentRepository.findByBookIdOrderByCreatedAtDesc(k2.getId());
        assertThat(beforeK1).hasSize(2);
        assertThat(beforeK2).hasSize(1);

        bookService.deleteById(k1.getId());

        assertThat(bookRepository.findById(k1.getId())).isEmpty();
        assertThat(commentRepository.findByBookIdOrderByCreatedAtDesc(k1.getId())).isEmpty();

        assertThat(commentRepository.findByBookIdOrderByCreatedAtDesc(k2.getId())).hasSize(1);
    }
}
