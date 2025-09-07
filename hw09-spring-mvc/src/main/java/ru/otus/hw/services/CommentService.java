package ru.otus.hw.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Comment findById(long id);

    List<Comment> findByBookId(long bookId);

    Page<Comment> findByBookId(long bookId, Pageable page);

    Comment insert(long bookId, String text);

    Comment update(long id, String text);

    void deleteById(long id);
}