package ru.otus.hw.repo.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.domain.mongo.MongoBook;

import java.util.List;
import java.util.Optional;

public interface MongoBookRepository extends MongoRepository<MongoBook, String> {
    List<MongoBook> findByAuthorId(String authorId);

    Optional<MongoBook> findByTitleAndAuthorId(String title, String authorId);

    boolean existsByAuthorId(String authorId);
}