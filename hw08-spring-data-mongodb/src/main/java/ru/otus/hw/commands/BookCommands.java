package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.services.BookService;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@RequiredArgsConstructor
@ShellComponent
public class BookCommands {

    private final BookService bookService;

    private final BookConverter bookConverter;

    @ShellMethod(value = "Find all books", key = "ab")
    public String findAllBooks() {
        var books = bookService.findAll();
        var header = bookConverter.headerWithCount(books.size());
        if (books.isEmpty()) {
            return header + System.lineSeparator() + "— nothing to show —";
        }
        var body = books.stream()
                .map(bookConverter::bookToString)
                .collect(Collectors.joining(System.lineSeparator()));
        return header + System.lineSeparator() + body;
    }

    @ShellMethod(value = "Find book by id", key = "bbid")
    public String findBookById(String id) {
        return bookService.findById(id)
                .map(bookConverter::bookToString)
                .orElse("Book with id %s not found".formatted(id));
    }

    @ShellMethod(value = "Insert book", key = "bins")
    public String insertBook(String title, String authorId, Set<String> genresIds) {
        var savedBook = bookService.insert(title, authorId, genresIds);
        return "Created:" + System.lineSeparator() + bookConverter.bookToString(savedBook);
    }

    @ShellMethod(value = "Update book", key = "bupd")
    public String updateBook(String id, String title, String authorId, Set<String> genresIds) {
        var savedBook = bookService.update(id, title, authorId, genresIds);
        return "Updated:" + System.lineSeparator() + bookConverter.bookToString(savedBook);
    }

    @ShellMethod(value = "Delete book by id", key = "bdel")
    public String deleteBook(String id) {
        bookService.deleteById(id);
        return "Deleted book %s (if existed)".formatted(id);
    }
}