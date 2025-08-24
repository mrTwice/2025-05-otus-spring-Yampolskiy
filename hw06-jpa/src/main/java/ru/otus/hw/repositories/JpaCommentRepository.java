package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class JpaCommentRepository implements CommentRepository {

    private final EntityManager entityManager;

    @Override
    public Optional<Comment> findById(long id) {
        return Optional.ofNullable(entityManager.find(Comment.class, id));
    }

    @Override
    public List<Comment> findByBookId(long bookId) {
        return entityManager.createQuery(
                        "select c from Comment c " +
                                "join fetch c.book b " +
                                "where b.id = :bookId " +
                                "order by c.createdAt desc", Comment.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    @Override
    public Comment save(Comment comment) {
        final Long id = comment.getId();
        if (id == null || id == 0L) {
            entityManager.persist(comment);
            return comment;
        }
        return entityManager.merge(comment);
    }

    @Override
    public void deleteById(long id) {
        Comment ref = entityManager.find(Comment.class, id);
        if (ref != null) {
            entityManager.remove(ref);
        }
    }
}