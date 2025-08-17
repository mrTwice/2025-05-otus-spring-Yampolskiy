package ru.otus.hw.repositories;

import java.util.Collection;
import java.util.List;

public interface ListCrudRepository<T, I> extends CrudRepository<T, I> {

    List<T> findAll();

    List<T> findAllById(Collection<I> ids);

    List<T> saveAll(Collection<T> entities);

    void deleteAllById(Collection<I> ids);

    void deleteAll(Collection<T> entities);

    void deleteAll();
}