package ru.otus.hw.library.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.library.models.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"book", "author"})
    List<Comment> findByBookIdOrderByCreatedAtDesc(Long bookId);

    @EntityGraph(attributePaths = {"book", "author"})
    Page<Comment> findByBookId(Long bookId, Pageable pageable);
}