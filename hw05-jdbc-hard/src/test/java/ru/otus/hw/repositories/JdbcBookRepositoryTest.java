package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Репозиторий на основе Jdbc для работы с книгами ")
@JdbcTest
@Import({JdbcBookRepository.class, JdbcGenreRepository.class})
class JdbcBookRepositoryTest {

    @Autowired
    private JdbcBookRepository repositoryJdbc;

    @Autowired
    private DataSource dataSource;

    private List<Author> dbAuthors;

    private List<Genre> dbGenres;

    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        dbAuthors = getDbAuthors();
        dbGenres = getDbGenres();
        dbBooks = getDbBooks(dbAuthors, dbGenres);
    }

    @DisplayName("должен загружать книгу по id")
    @ParameterizedTest
    @MethodSource("getDbBooks")
    void shouldReturnCorrectBookById(Book expectedBook) {
        var actualBook = repositoryJdbc.findById(expectedBook.getId());
        assertThat(actualBook).isPresent()
                .get()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = repositoryJdbc.findAll();
        var expectedBooks = dbBooks;

        assertThat(actualBooks).containsExactlyElementsOf(expectedBooks);
        actualBooks.forEach(System.out::println);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        var expectedBook = new Book(0, "BookTitle_10500", dbAuthors.get(0),
                List.of(dbGenres.get(0), dbGenres.get(2)));
        var returnedBook = repositoryJdbc.save(expectedBook);
        assertThat(returnedBook).isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBook);

        assertThat(repositoryJdbc.findById(returnedBook.getId()))
                .isPresent()
                .get()
                .isEqualTo(returnedBook);
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        var expectedBook = new Book(1L, "BookTitle_10500", dbAuthors.get(2),
                List.of(dbGenres.get(4), dbGenres.get(5)));

        assertThat(repositoryJdbc.findById(expectedBook.getId()))
                .isPresent()
                .get()
                .isNotEqualTo(expectedBook);

        var returnedBook = repositoryJdbc.save(expectedBook);
        assertThat(returnedBook).isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBook);

        assertThat(repositoryJdbc.findById(returnedBook.getId()))
                .isPresent()
                .get()
                .isEqualTo(returnedBook);
    }

    @DisplayName("должен удалять книгу по id ")
    @Test
    void shouldDeleteBook() {
        assertThat(repositoryJdbc.findById(1L)).isPresent();
        repositoryJdbc.deleteById(1L);
        assertThat(repositoryJdbc.findById(1L)).isEmpty();
    }

    @DisplayName("должен кидать EntityNotFoundException при обновлении несуществующей книги")
    @Test
    void shouldThrowOnUpdateNotExistingBook() {
        var notExisting = new Book(
                999_999L,
                "Nope",
                new Author(1L, "Author_1"),
                List.of(new Genre(1L, "Genre_1"))
        );

        assertThatThrownBy(() -> repositoryJdbc.save(notExisting))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("при ON DELETE CASCADE удаляет книгу и её связи без исключения")
    @Test
    void shouldDeleteBookAndCascadeRelations() {
        assertThat(repositoryJdbc.findById(1L)).isPresent();

        assertThatCode(() -> repositoryJdbc.deleteById(1L)).doesNotThrowAnyException();

        assertThat(repositoryJdbc.findById(1L)).isEmpty();

    }

    @DisplayName("должен молча завершаться при удалении несуществующей книги")
    @Test
    void shouldSilentlyIgnoreDeleteOfMissingId() {
        assertThatCode(() -> repositoryJdbc.deleteById(999_999L))
                .doesNotThrowAnyException();
    }

    @DisplayName("должен сохранять книгу без жанров")
    @Test
    void shouldSaveBookWithoutGenres() {
        var book = new Book(0L, "NoGenres", dbAuthors.get(0), List.of());
        var saved = repositoryJdbc.save(book);

        assertThat(saved.getId()).isPositive();
        assertThat(repositoryJdbc.findById(saved.getId()))
                .isPresent()
                .get()
                .satisfies(b -> assertThat(b.getGenres()).isEmpty());
    }

    @DisplayName("должен кидать DataIntegrityViolationException при дубликатах жанров (если есть UNIQUE)")
    @Test
    void shouldFailOnDuplicateGenresIfUnique() {
        var g = dbGenres.get(0);
        var book = new Book(0L, "DupGenres", dbAuthors.get(0), List.of(g, g));

        if (hasUniqueOnBookGenre()) {
            assertThatThrownBy(() -> repositoryJdbc.save(book))
                    .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        }
    }

    private boolean hasUniqueOnBookGenre() {
        try (var conn = dataSource.getConnection()) {
            var meta = conn.getMetaData();
            try (var rs = meta.getIndexInfo(null, null, "books_genres", true, false)) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    if ("book_id".equalsIgnoreCase(columnName) || "genre_id".equalsIgnoreCase(columnName)) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @DisplayName("должен перезаписывать жанры при обновлении книги")
    @Test
    void shouldOverwriteGenresOnUpdate() {
        var original = repositoryJdbc.findById(1L).orElseThrow();

        var newGenres = List.of(dbGenres.get(4), dbGenres.get(5)); // например, 5 и 6
        var updated = new Book(original.getId(), original.getTitle(), original.getAuthor(), newGenres);

        repositoryJdbc.save(updated);

        var reloaded = repositoryJdbc.findById(original.getId()).orElseThrow();
        assertThat(reloaded.getGenres())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(newGenres);
    }

    @DisplayName("findById должен возвращать книгу без жанров корректно")
    @Test
    void shouldReturnBookWithoutGenres() {
        var book = new Book(0L, "EmptyGenres", dbAuthors.get(1), List.of());
        var saved = repositoryJdbc.save(book);

        var loaded = repositoryJdbc.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getGenres()).isEmpty();
    }


    @DisplayName("должен загружать список всех книг с предсказуемым порядком книг")
    @Test
    void shouldReturnBooksInStableOrder() {
        var actual = repositoryJdbc.findAll();
        assertThat(actual).extracting(Book::getId).containsExactly(1L, 2L, 3L);

        assertThat(actual.get(0).getGenres())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(dbBooks.get(0).getGenres());
    }

    private static List<Author> getDbAuthors() {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Author(id, "Author_" + id))
                .toList();
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }

    private static List<Book> getDbBooks(List<Author> dbAuthors, List<Genre> dbGenres) {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Book(id,
                        "BookTitle_" + id,
                        dbAuthors.get(id - 1),
                        dbGenres.subList((id - 1) * 2, (id - 1) * 2 + 2)
                ))
                .toList();
    }

    private static List<Book> getDbBooks() {
        var dbAuthors = getDbAuthors();
        var dbGenres = getDbGenres();
        return getDbBooks(dbAuthors, dbGenres);
    }
}