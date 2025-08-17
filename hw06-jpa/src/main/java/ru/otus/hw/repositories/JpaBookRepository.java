package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBookRepository implements BookRepository {

    private final EntityManager em;

    @Override
    public Optional<Book> findById(long id) {
        var list = em.createQuery(
                        "select distinct b from Book b " +
                                "left join fetch b.author " +
                                "left join fetch b.genres " +
                                "where b.id = :id", Book.class)
                .setParameter("id", id)
                .getResultList();
        return list.stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        return em.createQuery(
                "select distinct b from Book b " +
                        "left join fetch b.author " +
                        "left join fetch b.genres " +
                        "order by b.id", Book.class
        ).getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == null) {
            em.persist(book);
            return book;
        }
        return em.merge(book);
    }

    @Override
    public void deleteById(long id) {
        Book ref = em.find(Book.class, id);
        if (ref != null) em.remove(ref);
    }
}
