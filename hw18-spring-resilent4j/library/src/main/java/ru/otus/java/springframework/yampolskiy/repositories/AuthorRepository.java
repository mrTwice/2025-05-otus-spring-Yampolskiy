package ru.otus.java.springframework.yampolskiy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.java.springframework.yampolskiy.models.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}