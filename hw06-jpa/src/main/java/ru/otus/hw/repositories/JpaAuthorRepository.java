package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

@Repository
public class JpaAuthorRepository extends AbstractJpaListCrudRepository<Author, Long>
        implements AuthorRepository {

    public JpaAuthorRepository(EntityManager em) {
        super(em, Author.class);
    }
}