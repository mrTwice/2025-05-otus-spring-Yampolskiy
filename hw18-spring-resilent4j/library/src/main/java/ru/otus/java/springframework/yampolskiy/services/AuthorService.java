package ru.otus.java.springframework.yampolskiy.services;

import ru.otus.java.springframework.yampolskiy.models.Author;

import java.util.List;

public interface AuthorService {
    List<Author> findAll();
}
