package ru.otus.hw.library.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.library.models.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}