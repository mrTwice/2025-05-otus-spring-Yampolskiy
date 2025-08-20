package ru.otus.hw.repositories;

import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository {
    Optional<Author> findById(long id);

    List<Author> findAll();

    Author save(Author author);

    void deleteById(long id);
}