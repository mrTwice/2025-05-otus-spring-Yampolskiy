package ru.otus.hw.library.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.otus.hw.library.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(attributePaths = {"author", "genres"})
    List<Book> findAll();

    @EntityGraph(attributePaths = {"author", "genres"})
    Optional<Book> findById(Long id);

    @Query(
            value = "select distinct b from Book b " +
                    "left join fetch b.author " +
                    "left join fetch b.genres",
            countQuery = "select count(b) from Book b"
    )
    Page<Book> findPageWithAuthorAndGenres(Pageable pageable);
}
