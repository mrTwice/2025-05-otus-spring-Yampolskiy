package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;

import java.util.stream.Collectors;


@RequiredArgsConstructor
@Component
public class BookConverter {

    public String bookToString(Book book) {
        if (book == null) {
            return "";
        }

        String id = book.getId() == null ? "-" : book.getId();
        String title = book.getTitle().trim();
        String authorId = book.getAuthorId().trim();

        String genres = (book.getGenreIds() == null || book.getGenreIds().isEmpty())
                ? "-"
                : book.getGenreIds().stream()
                .map(String::trim)
                .map("{%s}"::formatted)
                .collect(Collectors.joining(", "));

        return "%s  \"%s\"  authorId=%s  genres=[%s]"
                .formatted(id, title, authorId, genres);
    }

    public String headerWithCount(int count) {
        return "Books (%d)".formatted(count);
    }
}
