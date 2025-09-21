package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentForm;
import ru.otus.hw.dto.PageResponse;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.services.CommentService;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/books/{bookId}/comments")
@RequiredArgsConstructor
public class BookCommentsRestController {

    private final CommentService commentService;

    private final CommentMapper commentMapper;

    @GetMapping
    public PageResponse<CommentDto> list(@PathVariable long bookId, Pageable pageable) {
        return PageResponse.from(
                commentService.findByBookId(bookId, pageable).map(commentMapper::toDto)
        );
    }

    @PostMapping
    public ResponseEntity<CommentDto> create(@PathVariable long bookId,
                                             @Validated @RequestBody CommentForm form) {
        var saved = commentService.insert(bookId, form.getText());
        var dto = commentMapper.toDto(saved);
        URI location = URI.create("/api/v1/books/" + bookId + "/comments/" + saved.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long bookId, @PathVariable long commentId) {
        commentService.deleteById(commentId);
    }
}
