package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Comment;

import java.util.List;

@Repository
public class JpaCommentRepository extends AbstractJpaListCrudRepository<Comment, Long>
        implements CommentRepository {

    public JpaCommentRepository(EntityManager em) {
        super(em, Comment.class);
    }

    @Override
    public List<Comment> findByBookId(long bookId) {
        return em.createQuery(
                        "select c from Comment c " +
                                "join fetch c.book b " +
                                "where b.id = :bookId " +
                                "order by c.createdAt desc", Comment.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }
}