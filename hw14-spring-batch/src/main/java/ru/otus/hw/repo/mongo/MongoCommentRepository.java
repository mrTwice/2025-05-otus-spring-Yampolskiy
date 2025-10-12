package ru.otus.hw.repo.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.domain.mongo.MongoComment;

import java.util.List;

public interface MongoCommentRepository extends MongoRepository<MongoComment, String> {
    List<MongoComment> findByBookIdOrderByCreatedAtDesc(String bookId);

    long countByBookId(String bookId);

    long deleteByBookId(String bookId);
}