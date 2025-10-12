package ru.otus.hw.repo.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.domain.mongo.MongoGenre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MongoGenreRepository extends MongoRepository<MongoGenre, String> {
    List<MongoGenre> findByIdIn(Set<String> ids);

    Optional<MongoGenre> findByName(String name);

    boolean existsByName(String name);
}