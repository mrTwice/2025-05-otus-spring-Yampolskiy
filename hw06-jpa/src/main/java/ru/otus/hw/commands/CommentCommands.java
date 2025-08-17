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
    public String findCommentById(long id) {
        return commentService.findById(id)
                .map(commentConverter::commentToString)
                .orElse("Comment with id %d not found".formatted(id));
    }

    @ShellMethod(value = "Find comments by book id", key = "cbbid")
    public String findCommentsByBookId(long bookId) {
        var sep = "," + System.lineSeparator();
        return commentService.findByBookId(bookId).stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining(sep));
    }

    @ShellMethod(value = "Insert comment (bookId, text)", key = "cins")
    public String insertComment(long bookId, String text) {
        var saved = commentService.insert(bookId, text);
        return commentConverter.commentToString(saved);
    }

    @ShellMethod(value = "Update comment (id, text)", key = "cupd")
    public String updateComment(long id, String text) {
        var updated = commentService.update(id, text);
        return commentConverter.commentToString(updated);
    }

    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public void deleteComment(long id) {
        commentService.deleteById(id);
    }
}