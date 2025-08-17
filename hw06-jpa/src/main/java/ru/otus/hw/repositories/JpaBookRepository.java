package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaBookRepository extends AbstractJpaListCrudRepository<Book, Long>
        implements BookRepository {

    public JpaBookRepository(EntityManager em) {
        super(em, Book.class);
    }

    @Override
    public List<Book> findAll() {
        // автор и жанры подтягиваются одной пачкой; distinct убирает дубликаты
        return em.createQuery(
                        "select distinct b from Book b " +
                                "left join fetch b.author " +
                                "left join fetch b.genres " +
                                "order by b.id", Book.class)
                .getResultList();
    }

    @Override
    public Optional<Book> findById(Long id) {
        var list = em.createQuery(
                        "select distinct b from Book b " +
                                "left join fetch b.author " +
                                "left join fetch b.genres " +
                                "where b.id = :id", Book.class)
                .setParameter("id", id)
                .getResultList();
        return list.stream().findFirst();
    }
}