package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaAuthorRepository implements AuthorRepository {

    private final EntityManager em;

    @Override
    public List<Author> findAll() {
        return em.createQuery(
                "select a from Author a order by a.id", Author.class
        ).getResultList();
    }

    @Override
    public Optional<Author> findById(long id) {
        return Optional.ofNullable(em.find(Author.class, id));
    }
}