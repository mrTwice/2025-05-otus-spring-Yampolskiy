package ru.otus.hw.library.services;

import ru.otus.hw.library.models.Author;

import java.util.List;

public interface AuthorService {
    List<Author> findAll();
}
