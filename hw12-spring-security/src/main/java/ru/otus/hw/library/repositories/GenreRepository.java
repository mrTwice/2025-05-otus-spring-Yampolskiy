package ru.otus.hw.library.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.library.models.Genre;

import java.util.List;
import java.util.Set;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findByIdIn(Set<Long> ids);

    @Override
    Page<Genre> findAll(Pageable pageable);
}
