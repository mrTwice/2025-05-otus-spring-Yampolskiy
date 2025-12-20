package ru.otus.java.springframework.yampolskiy.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.java.springframework.yampolskiy.models.Comment;

import java.util.List;

public interface CommentService {
    Comment findById(long id);

    List<Comment> findByBookId(long bookId);

    Page<Comment> findByBookId(long bookId, Pageable page);

    Comment insert(long bookId, String text);

    void deleteById(long id);
}