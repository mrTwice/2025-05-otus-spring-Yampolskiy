package ru.otus.hw.library.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.library.models.Author;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByFullName(String fullName);
}