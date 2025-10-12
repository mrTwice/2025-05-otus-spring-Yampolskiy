package ru.otus.hw.migration.mongo2pg;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import ru.otus.hw.domain.pg.JpaBook;
import ru.otus.hw.domain.pg.JpaComment;

import java.util.List;

@RequiredArgsConstructor
public class CommentPgUpsertWriter implements ItemWriter<JpaComment> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void write(Chunk<? extends JpaComment> chunk) {
        for (JpaComment in : chunk) {
            Long bookId = in.getJpaBook() != null ? in.getJpaBook().getId() : null;
            if (bookId == null) {
                throw new IllegalStateException("JpaComment.book.id is required for upsert");
            }

            Long existingId = findCommentId(bookId, in.getCreatedAt(), in.getText());
            JpaComment entity = (existingId != null)
                    ? em.find(JpaComment.class, existingId)
                    : new JpaComment();

            entity.setJpaBook(em.getReference(JpaBook.class, bookId));
            entity.setCreatedAt(in.getCreatedAt());
            entity.setText(in.getText());
            entity.setVersion(in.getVersion());

            if (existingId == null) {
                em.persist(entity);
            }
        }
    }

    private Long findCommentId(Long bookId, java.time.Instant createdAt, String text) {
        TypedQuery<Long> q = em.createQuery(
                "select c.id from JpaComment c where c.jpaBook.id = :bid and c.createdAt = :ts and c.text = :txt",
                Long.class);
        q.setParameter("bid", bookId);
        q.setParameter("ts", createdAt);
        q.setParameter("txt", text);
        List<Long> res = q.setMaxResults(1).getResultList();
        return res.isEmpty() ? null : res.get(0);
    }
}
