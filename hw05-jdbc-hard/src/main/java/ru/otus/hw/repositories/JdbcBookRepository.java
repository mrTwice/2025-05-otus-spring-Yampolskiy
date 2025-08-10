package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashSet;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<Book> findById(long id) {
        String sql = """
            select b.id as b_id, b.title,
                   a.id as a_id, a.full_name as a_name,
                   bg.genre_id as g_id
            from books b
            join authors a on a.id = b.author_id
            left join books_genres bg on bg.book_id = b.id
            where b.id = :id
            order by b.id
        """;
        Book book = jdbc.query(sql, Map.of("id", id), new BookResultSetExtractor());
        if (book == null) {
            return Optional.empty();
        }

        Set<Long> gIds = book.getGenres() == null ? Set.of()
                : book.getGenres().stream().map(Genre::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        var genres = genreRepository.findAllByIds(gIds);
        book.setGenres(genres);
        return Optional.of(book);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var relations = getAllGenreRelations();
        var books = getAllBooksWithoutGenres();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        jdbc.update("delete from books where id=:id", Map.of("id", id));
    }

    private List<Book> getAllBooksWithoutGenres() {
        String sql = """
            select b.id as b_id, b.title,
                   a.id as a_id, a.full_name as a_name
            from books b
            join authors a on a.id = b.author_id
            order by b.id
        """;
        return jdbc.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        String sql = "select book_id, genre_id from books_genres order by book_id, genre_id";
        return jdbc.query(sql, (rs, i) -> new BookGenreRelation(rs.getLong("book_id"), rs.getLong("genre_id")));
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        if (booksWithoutGenres.isEmpty() || relations.isEmpty() || genres.isEmpty()) {
            return;
        }

        var byId = booksWithoutGenres.stream().collect(Collectors.toMap(Book::getId, b -> b));
        var genreById = genres.stream().collect(Collectors.toMap(Genre::getId, g -> g));

        for (var rel : relations) {
            var b = byId.get(rel.bookId());
            var g = genreById.get(rel.genreId());
            if (b != null && g != null) {
                if (b.getGenres() == null) {
                    b.setGenres(new ArrayList<>());
                }
                b.getGenres().add(g);
            }
        }
        booksWithoutGenres.forEach(b -> {
            if (b.getGenres() == null) {
                b.setGenres(new ArrayList<>());
            }
        });
    }

    private Book insert(Book book) {
        var kh = new GeneratedKeyHolder();
        String sql = "insert into books(title, author_id) values(:title, :authorId)";
        jdbc.update(sql,
                new MapSqlParameterSource()
                        .addValue("title", book.getTitle())
                        .addValue("authorId", book.getAuthor().getId()),
                kh, new String[]{"id"});

        book.setId(kh.getKeyAs(Long.class));
        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        String sql = "update books set title=:title, author_id=:authorId where id=:id";
        int updated = jdbc.update(sql, Map.of(
                "id", book.getId(),
                "title", book.getTitle(),
                "authorId", book.getAuthor().getId()
        ));
        if (updated == 0) {
            throw new EntityNotFoundException("Book with id=" + book.getId() + " not found");
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        var gs = book.getGenres();
        if (gs == null || gs.isEmpty()) {
            return;
        }

        String sql = "insert into books_genres(book_id, genre_id) values(:bookId, :genreId)";

        MapSqlParameterSource[] batch = gs.stream()
                .map(g -> new MapSqlParameterSource()
                        .addValue("bookId", book.getId())
                        .addValue("genreId", g.getId()))
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate(sql, batch);
    }

    private void removeGenresRelationsFor(Book book) {
        jdbc.update("delete from books_genres where book_id=:id", Map.of("id", book.getId()));
    }

    private static class BookRowMapper implements RowMapper<Book> {
        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            var author = new Author(rs.getLong("a_id"), rs.getString("a_name"));
            return new Book(rs.getLong("b_id"), rs.getString("title"), author, new ArrayList<>());
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {
        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            Set<Long> genreIds = new LinkedHashSet<>();
            while (rs.next()) {
                if (book == null) {
                    var author = new Author(rs.getLong("a_id"), rs.getString("a_name"));
                    book = new Book(rs.getLong("b_id"), rs.getString("title"), author, new ArrayList<>());
                }
                long gid = rs.getLong("g_id");
                if (!rs.wasNull()) {
                    genreIds.add(gid);
                }
            }
            if (book != null) {
                var stubGenres = genreIds.stream().map(id -> new Genre(id, null)).toList();
                book.setGenres(new ArrayList<>(stubGenres));
            }
            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
