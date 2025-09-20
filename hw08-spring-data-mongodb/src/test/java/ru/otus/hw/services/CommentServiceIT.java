package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.config.DataSeeder;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CommentServiceIT {

    @Autowired
    private CommentService commentService;

    @Autowired
    private DataSeeder dataSeeder;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private String book1Id;
    private String book2Id;


    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        dataSeeder.seed();

        var books = bookRepository.findAll();
        var k1 = books.stream().filter(b -> "BookTitle_1".equals(b.getTitle())).findFirst().orElseThrow();
        var k2 = books.stream().filter(b -> "BookTitle_2".equals(b.getTitle())).findFirst().orElseThrow();

        book1Id = k1.getId();
        book2Id = k2.getId();
    }

    @Test
    @DisplayName("findById: возвращает комментарий, если он существует")
    void findById_exists() {
        var c = commentRepository.save(new Comment(null, "hello", null, book1Id));

        Optional<Comment> found = commentService.findById(c.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("hello");
        assertThat(found.get().getBookId()).isEqualTo(book1Id);
    }

    @Test
    @DisplayName("findById: пустой результат для несуществующего id")
    void findById_missing() {
        assertThat(commentService.findById("missing")).isEmpty();
    }


    @Test
    @DisplayName("findByBookId: bookId пустой/blank → ValidationException")
    void findByBookId_blank() {
        assertThatThrownBy(() -> commentService.findByBookId("  "))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Book id must not be null or blank");
        assertThatThrownBy(() -> commentService.findByBookId(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("findByBookId: книга не существует → EntityNotFoundException")
    void findByBookId_bookNotFound() {
        assertThatThrownBy(() -> commentService.findByBookId("missing"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id missing not found");
    }

    @Test
    @DisplayName("insert: happy-path — тримит текст, проставляет createdAt, сохраняет")
    void insert_happyPath() {
        var saved = commentService.insert(book1Id, "  New comment  ");

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getText()).isEqualTo("New comment");
        assertThat(saved.getBookId()).isEqualTo(book1Id);
        assertThat(saved.getCreatedAt()).isNotNull();

        var inDb = commentRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getId()).isEqualTo(saved.getId());
        assertThat(inDb.getText()).isEqualTo(saved.getText());
        assertThat(inDb.getBookId()).isEqualTo(saved.getBookId());
        assertThat(inDb.getCreatedAt())
                .isCloseTo(saved.getCreatedAt(), within(1, ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("insert: text пустой/blank → ValidationException")
    void insert_blankText() {
        assertThatThrownBy(() -> commentService.insert(book1Id, "   "))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Comment text must not be null or blank");
        assertThatThrownBy(() -> commentService.insert(book1Id, null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("insert: bookId пустой/blank → ValidationException")
    void insert_blankBookId() {
        assertThatThrownBy(() -> commentService.insert("  ", "text"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Book id must not be null or blank");
        assertThatThrownBy(() -> commentService.insert(null, "text"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("insert: книга не существует → EntityNotFoundException")
    void insert_bookNotFound() {
        assertThatThrownBy(() -> commentService.insert("missing", "text"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id missing not found");
    }


    @Test
    @DisplayName("update: text пустой/blank → ValidationException")
    void update_blankText() {
        var c = commentRepository.save(new Comment(null, "x", null, book1Id));
        assertThatThrownBy(() -> commentService.update(c.getId(), "   "))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Comment text must not be null or blank");
        assertThatThrownBy(() -> commentService.update(c.getId(), null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("update: комментарий не найден → EntityNotFoundException")
    void update_notFound() {
        assertThatThrownBy(() -> commentService.update("missing", "new"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id missing not found");
    }

    @Test
    @DisplayName("deleteById: удаляет существующий комментарий, остальные остаются")
    void deleteById_deletesOnlyTarget() {
        var c1 = commentRepository.save(new Comment(null, "c1", Instant.now().minusSeconds(3), book1Id));
        var c2 = commentRepository.save(new Comment(null, "c2", Instant.now().minusSeconds(2), book1Id));
        var cOtherBook = commentRepository.save(new Comment(null, "other", Instant.now().minusSeconds(1), book2Id));

        commentService.deleteById(c1.getId());

        assertThat(commentRepository.findById(c1.getId())).isEmpty();
        assertThat(commentRepository.findById(c2.getId())).isPresent();
        assertThat(commentRepository.findById(cOtherBook.getId())).isPresent();
    }

    @Test
    @DisplayName("deleteById: несуществующий id → no-op")
    void deleteById_missing_noop() {
        var c = commentRepository.save(new Comment(null, "x", null, book1Id));
        long before = commentRepository.count();

        commentService.deleteById("missing");

        assertThat(commentRepository.count()).isEqualTo(before);
        assertThat(commentRepository.findById(c.getId())).isPresent();
    }
}
