package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final AuthorRepository authors;

    private final GenreRepository genres;

    private final BookRepository books;

    private final CommentRepository comments;

    public void seed() {
        if (authors.count() > 0) {
            return;
        }
        var savedAuthors = seedAuthors();
        var savedGenres = seedGenres();
        var savedBooks = seedBooks(savedAuthors, savedGenres);
        seedComments(savedBooks);
    }

    private List<Author> seedAuthors() {
        List<Author> list = new ArrayList<>();
        list.add(authors.save(new Author(null, "Author_1")));
        list.add(authors.save(new Author(null, "Author_2")));
        list.add(authors.save(new Author(null, "Author_3")));
        return list;
    }

    private List<Genre> seedGenres() {
        return genres.saveAll(List.of(
                new Genre(null, "Genre_1"), new Genre(null, "Genre_2"),
                new Genre(null, "Genre_3"), new Genre(null, "Genre_4"),
                new Genre(null, "Genre_5"), new Genre(null, "Genre_6")
        ));
    }

    private List<Book> seedBooks(List<Author> authors, List<Genre> genres) {
        var g1 = genres.get(0).getId();
        var g2 = genres.get(1).getId();
        var g3 = genres.get(2).getId();
        var g4 = genres.get(3).getId();
        var g5 = genres.get(4).getId();
        var g6 = genres.get(5).getId();

        return List.of(
                books.save(new Book(null, "BookTitle_1", authors.get(0).getId(), List.of(g1, g2))),
                books.save(new Book(null, "BookTitle_2", authors.get(1).getId(), List.of(g3, g4))),
                books.save(new Book(null, "BookTitle_3", authors.get(2).getId(), List.of(g5, g6)))
        );
    }

    private void seedComments(List<Book> books) {
        comments.saveAll(List.of(
                new Comment(null, "Great book!", null, books.get(0).getId()),
                new Comment(null, "Not my cup of tea", null, books.get(0).getId()),
                new Comment(null, "Awesome read", null, books.get(1).getId())
        ));
    }
}