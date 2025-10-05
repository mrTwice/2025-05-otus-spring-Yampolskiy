package ru.otus.hw.library.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.hw.library.models.Genre;

import java.util.List;

public interface GenreService {
    List<Genre> findAll();

    Page<Genre> findAll(Pageable page);
}
