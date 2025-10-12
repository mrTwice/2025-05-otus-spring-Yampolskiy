package ru.otus.hw.repo.pg;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.domain.pg.JpaComment;

import java.util.List;

public interface JpaCommentRepository extends JpaRepository<JpaComment, Long> {

    @EntityGraph(attributePaths = {"JpaBook"})
    List<JpaComment> findByJpaBookIdOrderByCreatedAtDesc(Long bookId);

    @EntityGraph(attributePaths = {"jpaBook"})
    Page<JpaComment> findByJpaBookId(Long bookId, Pageable pageable);
}