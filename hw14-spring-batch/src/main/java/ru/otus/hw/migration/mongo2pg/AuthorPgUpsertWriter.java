package ru.otus.hw.migration.mongo2pg;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import ru.otus.hw.domain.pg.JpaAuthor;
import ru.otus.hw.migration.cache.IdMappingService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AuthorPgUpsertWriter implements ItemWriter<JpaAuthor> {

    private final IdMappingService idMapping;

    @PersistenceContext
    private EntityManager em;

    @Override
    public void write(Chunk<? extends JpaAuthor> chunk) {
        recordMappingsAfterFlush(() -> {
            List<Runnable> afterFlush = new ArrayList<>(chunk.size());
            for (JpaAuthor in : chunk) {
                Long existingId = findIdByFullName(in.getFullName());
                JpaAuthor entity = (existingId != null)
                        ? em.find(JpaAuthor.class, existingId)
                        : new JpaAuthor();

                entity.setFullName(in.getFullName());
                entity.setVersion(in.getVersion());

                if (existingId == null) {
                    em.persist(entity);
                }

                afterFlush.add(() ->
                        idMapping.rememberPgAuthor(entity.getFullName(), entity.getId())
                );
            }
            return afterFlush;
        });
    }

    private Long findIdByFullName(String fullName) {
        TypedQuery<Long> q = em.createQuery(
                "select a.id from JpaAuthor a where a.fullName = :fn", Long.class);
        q.setParameter("fn", fullName);
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
