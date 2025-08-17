package ru.otus.hw.repositories;

import java.util.Optional;

public interface CrudRepository<T, I> {

    Optional<T> findById(I id);

    T save(T entity);

    void deleteById(I id);

    void delete(T entity);

    boolean existsById(I id);

    long count();
}