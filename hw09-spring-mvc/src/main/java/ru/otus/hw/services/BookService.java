package ru.otus.hw.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookService {
    Book findById(long id);

    List<Book> findAll();

    Book insert(String title, long authorId, Set<Long> genresIds);

    Book update(long id, String title, long authorId, Set<Long> genresIds);

    void deleteById(long id);

    Page<Book> findAll(Pageable pageable);
}
