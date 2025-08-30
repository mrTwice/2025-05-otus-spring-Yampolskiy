package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends MongoRepository<Genre, String> {

    Optional<Genre> findByName(String name);

    boolean existsByName(String name);

    List<Genre> findByIdIn(Collection<String> ids);
}