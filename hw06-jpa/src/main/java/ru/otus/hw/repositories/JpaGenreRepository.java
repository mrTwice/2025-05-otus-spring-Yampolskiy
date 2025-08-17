package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JpaGenreRepository implements GenreRepository {

    private final EntityManager em;

    @Override
    public List<Genre> findAll() {
        return em.createQuery(
                "select g from Genre g order by g.id", Genre.class
        ).getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return em.createQuery(
                "select g from Genre g where g.id in :ids order by g.id", Genre.class
        ).setParameter("ids", ids).getResultList();
    }
}