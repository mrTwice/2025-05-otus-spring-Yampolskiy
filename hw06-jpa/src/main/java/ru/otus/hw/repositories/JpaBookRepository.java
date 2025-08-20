package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class JpaBookRepository implements BookRepository  {

    private final EntityManager entityManager;

    @Override
    public List<Book> findAll() {
        return entityManager.createQuery(
                        "select distinct b from Book b " +
                                "left join fetch b.author " +
                                "left join fetch b.genres " +
                                "order by b.id", Book.class)
                .getResultList();
    }

    @Override
    public Optional<Book> findById(long id) {
        var list = entityManager.createQuery(
                        "select distinct b from Book b " +
                                "left join fetch b.author " +
                                "left join fetch b.genres " +
                                "where b.id = :id", Book.class)
                .setParameter("id", id)
                .getResultList();
        return list.stream().findFirst();
    }

    @Override
    public Book save(Book book) {
        final Long id = book.getId();
        if (id == null || id == 0L) {
            entityManager.persist(book);
            return book;
        }
        return entityManager.merge(book);
    }

    @Override
    public void deleteById(long id) {
        Book ref = entityManager.find(Book.class, id);
        if (ref != null) {
            entityManager.remove(ref);
        }
    }
}