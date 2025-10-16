package ru.otus.hw.repo.pg;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.otus.hw.domain.pg.JpaBook;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JpaBookRepository extends JpaRepository<JpaBook, Long> {

    @Override
    @EntityGraph(attributePaths = {"jpaAuthor", "jpaGenres"})
    List<JpaBook> findAll();

    @EntityGraph(attributePaths = {"jpaAuthor", "jpaGenres"})
    Optional<JpaBook> findById(Long id);

    @Query(value = "select b.id from JpaBook b",
            countQuery = "select count(b) from JpaBook b")
    Page<Long> findIdsPage(Pageable pageable);

    @EntityGraph(attributePaths = {"jpaAuthor", "jpaGenres"})
    List<JpaBook> findByIdIn(Collection<Long> ids);
}