package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.models.Comment;

import java.time.format.DateTimeFormatter;

@Component
public class CommentConverter {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(java.time.ZoneId.systemDefault());

    public String commentToString(Comment comment) {
        if (comment == null) {
            return "";
        }

        String id = comment.getId() == null ? "-" : comment.getId();
        String text = comment.getText().trim();
        String bookId = comment.getBookId().trim();

        String createdAt = comment.getCreatedAt() != null
                ? DATE_FORMATTER.format(comment.getCreatedAt())
                : "-";

        return "%s  \"%s\"  bookId=%s  createdAt=%s"
                .formatted(id, text, bookId, createdAt);
    }

    public String headerWithCount(int count) {
        return "Comments (%d)".formatted(count);
    }
}