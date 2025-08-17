package ru.otus.hw.repositories;

import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends ListCrudRepository<Book, Long> {
}
