package ru.otus.hw.repo.mongo;


import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.domain.mongo.MongoAuthor;

import java.util.Optional;

public interface MongoAuthorRepository extends MongoRepository<MongoAuthor, String> {
    boolean existsByFullName(String fullName);

    Optional<MongoAuthor> findByFullName(String fullName);
}