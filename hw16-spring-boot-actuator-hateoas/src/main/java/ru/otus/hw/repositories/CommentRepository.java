package ru.otus.hw.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.models.Comment;

import java.util.List;

@RepositoryRestResource(
        collectionResourceRel = "comments",
        itemResourceRel = "comment",
        path = "comments"
)
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"book"})
    List<Comment> findByBookIdOrderByCreatedAtDesc(Long bookId);

    @EntityGraph(attributePaths = {"book"})
    Page<Comment> findByBookId(Long bookId, Pageable pageable);
}