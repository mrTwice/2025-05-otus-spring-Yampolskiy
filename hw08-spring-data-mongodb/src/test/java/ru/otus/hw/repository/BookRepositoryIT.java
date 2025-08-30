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
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
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
@EnableMongoRepositories(basePackageClasses = BookRepository.class)
@Import(DataSeeder.class)
class BookRepositoryIT {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private CommentRepository commentRepository;


    @Autowired
    private DataSeeder seeder;

    private String author1Id;
    private String author2Id;

    @BeforeEach
    void setUp() {

        commentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        seeder.seed();

        author1Id = authorRepository.findByFullName("Author_1").orElseThrow().getId();
        author2Id = authorRepository.findByFullName("Author_2").orElseThrow().getId();
    }

    @Test
    @DisplayName("findByTitleAndAuthorId: находит книгу по {title, authorId} из сидера")
    void findByTitleAndAuthorId_found() {
        assertThat(bookRepository.findByTitleAndAuthorId("BookTitle_1", author1Id))
                .isPresent()
                .get()
                .extracting(Book::getTitle)
                .isEqualTo("BookTitle_1");

        assertThat(bookRepository.findByTitleAndAuthorId("BookTitle_1", author2Id))
                .isNotPresent();
    }

    @Test
    @DisplayName("findByAuthorId: возвращает все книги автора")
    void findByAuthorId_returnsBooksOfAuthor() {
        List<Book> a1Books = bookRepository.findByAuthorId(author1Id);
        assertThat(a1Books).hasSize(1);
        assertThat(a1Books.get(0).getAuthorId()).isEqualTo(author1Id);

        List<Book> a2Books = bookRepository.findByAuthorId(author2Id);
        assertThat(a2Books).hasSize(1);
        assertThat(a2Books.get(0).getAuthorId()).isEqualTo(author2Id);
    }

    @Test
    @DisplayName("existsByAuthorId: true для автора с книгами и false для автора без книг")
    void existsByAuthorId_trueFalse() {
        assertThat(bookRepository.existsByAuthorId(author1Id)).isTrue();

        String newAuthorId = authorRepository.save(new Author(null, "Author_X")).getId();
        assertThat(bookRepository.existsByAuthorId(newAuthorId)).isFalse();
    }

    @Test
    @DisplayName("unique {title, authorId}: попытка вставить дубликат пары бросает DuplicateKeyException")
    void unique_titleAuthor_duplicate() {
        String anyGenreId = genreRepository.findAll().get(0).getId();

        Book dup = new Book(null, "BookTitle_1", author1Id, List.of(anyGenreId));
        assertThatThrownBy(() -> bookRepository.save(dup))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
