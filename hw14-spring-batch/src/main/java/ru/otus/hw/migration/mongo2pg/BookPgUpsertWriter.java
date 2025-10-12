package ru.otus.hw.migration.mongo2pg;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import ru.otus.hw.domain.pg.JpaAuthor;
import ru.otus.hw.domain.pg.JpaBook;
import ru.otus.hw.domain.pg.JpaGenre;
import ru.otus.hw.migration.cache.IdMappingService;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BookPgUpsertWriter implements ItemWriter<JpaBook> {

    private final IdMappingService idMapping;

    @PersistenceContext
    private EntityManager em;

    @Override
    public void write(Chunk<? extends JpaBook> chunk) {
        recordMappingsAfterFlush(() -> processChunk(chunk));
    }

    private List<Runnable> processChunk(Chunk<? extends JpaBook> chunk) {
        List<Runnable> afterFlush = new ArrayList<>(chunk.size());
        for (JpaBook in : chunk) {
            afterFlush.add(processBook(in));
        }
        return afterFlush;
    }

    private Runnable processBook(JpaBook in) {
        Long authorId = requireAuthorId(in);
        Long existingId = findBookIdByAuthorAndTitle(authorId, in.getTitle());

        JpaBook entity = getOrCreateBook(existingId);
        JpaAuthor managedAuthor = em.getReference(JpaAuthor.class, authorId);

        updateBookEntity(entity, in, managedAuthor);
        persistIfNew(existingId, entity);

        String authorFullName = resolveAuthorFullName(in, authorId);
        return () -> idMapping.rememberPgBook(authorFullName, entity.getTitle(), entity.getId());
    }

    private Long requireAuthorId(JpaBook in) {
        Long authorId = in.getJpaAuthor() != null ? in.getJpaAuthor().getId() : null;
        if (authorId == null) {
            throw new IllegalStateException("JpaBook.author.id is required for upsert");
        }
        return authorId;
    }

    private JpaBook getOrCreateBook(Long existingId) {
        return (existingId != null)
                ? em.find(JpaBook.class, existingId)
                : new JpaBook();
    }

    private void updateBookEntity(JpaBook entity, JpaBook source, JpaAuthor managedAuthor) {
        entity.setJpaAuthor(managedAuthor);
        entity.setTitle(source.getTitle());
        entity.setVersion(source.getVersion());
        entity.replaceGenres(resolveManagedGenres(source));
    }

    private Set<JpaGenre> resolveManagedGenres(JpaBook source) {
        return Optional.ofNullable(source.getJpaGenres())
                .orElse(Collections.emptySet())
                .stream()
                .map(g -> em.getReference(JpaGenre.class, g.getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void persistIfNew(Long existingId, JpaBook entity) {
        if (existingId == null) {
            em.persist(entity);
        }
    }

    private String resolveAuthorFullName(JpaBook in, Long authorId) {
        if (in.getJpaAuthor() != null && in.getJpaAuthor().getFullName() != null) {
            return in.getJpaAuthor().getFullName();
        }
        return findAuthorFullNameById(authorId);
    }

    private Long findBookIdByAuthorAndTitle(Long authorId, String title) {
        TypedQuery<Long> q = em.createQuery(
                "select b.id from JpaBook b where b.jpaAuthor.id = :aid and b.title = :title", Long.class);
        q.setParameter("aid", authorId);
        q.setParameter("title", title);
        List<Long> res = q.setMaxResults(1).getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    private String findAuthorFullNameById(Long authorId) {
        TypedQuery<String> q = em.createQuery(
                "select a.fullName from JpaAuthor a where a.id = :id", String.class);
        q.setParameter("id", authorId);
        List<String> res = q.setMaxResults(1).getResultList();
        return res.isEmpty() ? "" : res.get(0);
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
