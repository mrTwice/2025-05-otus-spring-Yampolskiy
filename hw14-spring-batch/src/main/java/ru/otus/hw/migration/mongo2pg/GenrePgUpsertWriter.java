package ru.otus.hw.migration.mongo2pg;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import ru.otus.hw.domain.pg.JpaGenre;
import ru.otus.hw.migration.cache.IdMappingService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GenrePgUpsertWriter implements ItemWriter<JpaGenre> {

    private final IdMappingService idMapping;

    @PersistenceContext
    private EntityManager em;

    @Override
    public void write(Chunk<? extends JpaGenre> chunk) {
        recordMappingsAfterFlush(() -> {
            List<Runnable> afterFlush = new ArrayList<>(chunk.size());
            for (JpaGenre in : chunk) {
                Long existingId = findIdByName(in.getName());
                JpaGenre entity = (existingId != null)
                        ? em.find(JpaGenre.class, existingId)
                        : new JpaGenre();

                entity.setName(in.getName());
                entity.setVersion(in.getVersion());

                if (existingId == null) {
                    em.persist(entity);
                }

                afterFlush.add(() ->
                        idMapping.rememberPgGenre(entity.getName(), entity.getId())
                );
            }
            return afterFlush;
        });
    }

    private Long findIdByName(String name) {
        TypedQuery<Long> q = em.createQuery(
                "select g.id from JpaGenre g where g.name = :name", Long.class);
        q.setParameter("name", name);
        List<Long> res = q.setMaxResults(1).getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    private void recordMappingsAfterFlush(PostActionsSupplier supplier) {
        List<Runnable> afterFlush = supplier.get();
        em.flush();
        for (Runnable r : afterFlush) {
            r.run();
        }
    }

    @FunctionalInterface
    private interface PostActionsSupplier {
        List<Runnable> get();
    }
}
