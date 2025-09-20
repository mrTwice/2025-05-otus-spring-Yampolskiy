package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.CommentConverter;
import ru.otus.hw.services.CommentService;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
public class CommentCommands {

    private final CommentService commentService;

    private final CommentConverter commentConverter;

    @ShellMethod(value = "Find comment by id", key = "cbid")
    public String findCommentById(String id) {
        return commentService.findById(id)
                .map(commentConverter::commentToString)
                .orElse("Comment with id %s not found".formatted(id));
    }

    @ShellMethod(value = "Find comments by book id", key = "cbbid")
    public String findCommentsByBookId(String bookId) {
        var comments = commentService.findByBookId(bookId);
        var header = commentConverter.headerWithCount(comments.size());
        if (comments.isEmpty()) {
            return header + System.lineSeparator() + "— nothing to show —";
        }
        var body = comments.stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining(System.lineSeparator()));
        return header + System.lineSeparator() + body;
    }

    @ShellMethod(value = "Insert comment (bookId, text)", key = "cins")
    public String insertComment(String bookId, String text) {
        var saved = commentService.insert(bookId, text);
        return "Created:" + System.lineSeparator() + commentConverter.commentToString(saved);
    }

    @ShellMethod(value = "Update comment (id, text)", key = "cupd")
    public String updateComment(String id, String text) {
        var updated = commentService.update(id, text);
        return "Updated:" + System.lineSeparator() + commentConverter.commentToString(updated);
    }

    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public String deleteComment(String id) {
        commentService.deleteById(id);
        return "Deleted comment %s (if existed)".formatted(id);
    }
}