package ru.otus.java.springframework.yampolskiy.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.java.springframework.yampolskiy.models.Genre;

import java.util.List;

public interface GenreService {
    List<Genre> findAll();

    Page<Genre> findAll(Pageable page);
}
