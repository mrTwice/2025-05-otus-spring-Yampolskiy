package ru.otus.hw.repositories;

import ru.otus.hw.models.Comment;

import java.util.List;

public interface CommentRepository extends ListCrudRepository<Comment, Long> {
    List<Comment> findByBookId(long bookId);
}