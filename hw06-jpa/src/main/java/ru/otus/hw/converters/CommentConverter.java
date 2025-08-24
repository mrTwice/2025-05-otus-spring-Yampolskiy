package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.models.Comment;

@Component
public class CommentConverter {

    public String commentToString(Comment comment) {
        Long bookId = comment.getBook() != null ? comment.getBook().getId() : null;
        String createdAt = comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : "";
        return "Id: %d, Text: %s, BookId: %s, CreatedAt: %s"
                .formatted(comment.getId(), comment.getText(), String.valueOf(bookId), createdAt);
    }

    public String commentToStringWithBook(Comment comment) {
        var book = comment.getBook();
        String bookInfo = (book != null)
                ? "Book{id=%d, title=%s}".formatted(book.getId(), book.getTitle())
                : "Book{null}";
        String createdAt = comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : "";
        return "Id: %d, Text: %s, %s, CreatedAt: %s"
                .formatted(comment.getId(), comment.getText(), bookInfo, createdAt);
    }
}