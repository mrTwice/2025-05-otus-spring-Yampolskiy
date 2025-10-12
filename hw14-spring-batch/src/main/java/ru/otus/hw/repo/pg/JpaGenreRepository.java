package ru.otus.hw.repo.pg;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.domain.pg.JpaGenre;

import java.util.List;
import java.util.Set;

public interface JpaGenreRepository extends JpaRepository<JpaGenre, Long> {
    List<JpaGenre> findByIdIn(Set<Long> ids);

    @Override
    Page<JpaGenre> findAll(Pageable pageable);
}