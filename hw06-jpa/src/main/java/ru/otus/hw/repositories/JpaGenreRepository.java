package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

@Repository
public class JpaGenreRepository extends AbstractJpaListCrudRepository<Genre, Long>
        implements GenreRepository {

    private final EntityManager entityManager;

    public JpaGenreRepository(EntityManager em) {
        super(em, Genre.class);
        this.entityManager = em;
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
}