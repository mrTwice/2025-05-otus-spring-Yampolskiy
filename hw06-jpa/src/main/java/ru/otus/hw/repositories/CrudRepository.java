package ru.otus.hw.repositories;

import java.util.Optional;

public interface CrudRepository<T, ID> {
    Optional<T> findById(ID id);
    T save(T entity);
    void deleteById(ID id);
    void delete(T entity);
    boolean existsById(ID id);
    long count();
}