package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class JpaGenreRepository implements GenreRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Genre> findById(long id) {
        return Optional.ofNullable(entityManager.find(Genre.class, id));
    }

    @Override
    public List<Genre> findAll() {
        return entityManager.createQuery("select g from Genre g order by g.id", Genre.class)
                .getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return entityManager.createQuery("select g from Genre g where g.id in :ids order by g.id", Genre.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public Genre save(Genre genre) {
        if (genre.getId() == 0) {
            entityManager.persist(genre);
            return genre;
        }
        return entityManager.merge(genre);
    }

    @Override
    public void deleteById(long id) {
        Genre ref = entityManager.find(Genre.class, id);
        if (ref != null) {
            entityManager.remove(ref);
        }
    }
}