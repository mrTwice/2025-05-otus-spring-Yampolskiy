package ru.otus.hw.repositories;

import java.util.Collection;
import java.util.List;

public interface ListCrudRepository<T, ID> extends CrudRepository<T, ID> {
    List<T> findAll();
    List<T> findAllById(Collection<ID> ids);
    List<T> saveAll(Collection<T> entities);
    void deleteAllById(Collection<ID> ids);
    void deleteAll(Collection<T> entities);
    void deleteAll();
}