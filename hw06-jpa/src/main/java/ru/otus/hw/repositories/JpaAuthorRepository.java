package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaAuthorRepository implements AuthorRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Author> findById(long id) {
        return Optional.ofNullable(entityManager.find(Author.class, id));
    }

    @Override
    public List<Author> findAll() {
        return entityManager.createQuery("select a from Author a order by a.id", Author.class)
                .getResultList();
    }

    @Override
    public Author save(Author author) {
        final Long id = author.getId();
        if (id == null || id == 0L) {
            entityManager.persist(author);
            return author;
        }
        return entityManager.merge(author);
    }

    @Override
    public void deleteById(long id) {
        Author ref = entityManager.find(Author.class, id);
        if (ref != null) {
            entityManager.remove(ref);
        }
    }
}