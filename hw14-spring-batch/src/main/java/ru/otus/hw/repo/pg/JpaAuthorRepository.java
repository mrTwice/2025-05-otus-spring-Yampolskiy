package ru.otus.hw.repo.pg;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.domain.pg.JpaAuthor;

public interface JpaAuthorRepository extends JpaRepository<JpaAuthor, Long> {
}