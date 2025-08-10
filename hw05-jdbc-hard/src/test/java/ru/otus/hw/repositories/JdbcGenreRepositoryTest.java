package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JdbcGenreRepository (@JdbcTest)")
@JdbcTest
@Import(JdbcGenreRepository.class)
class JdbcGenreRepositoryTest {

    @Autowired
    private JdbcGenreRepository repo;

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("возвращает все жанры из БД в порядке по id")
        void returnsAllSeededGenresInIdOrder() {
            List<Genre> all = repo.findAll();

            assertThat(all).hasSize(6);
            assertThat(all).extracting(Genre::getId)
                    .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
            assertThat(all).extracting(Genre::getName)
                    .containsExactly("Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5", "Genre_6");
        }
    }

    @Nested
    @DisplayName("findAllByIds(Set<Long>)")
    class FindAllByIds {

        @Test
        @DisplayName("возвращает пустой список при пустом наборе id")
        void emptySetReturnsEmptyList() {
            assertThat(repo.findAllByIds(Set.of())).isEmpty();
        }

        @Test
        @DisplayName("возвращает пустой список при null")
        void nullReturnsEmptyList() {
            assertThat(repo.findAllByIds(null)).isEmpty();
        }

        @Test
        @DisplayName("возвращает только существующие жанры; порядок по id")
        void returnsOnlyExistingIdsInIdOrder() {
            Set<Long> ids = new LinkedHashSet<>(Set.of(5L, 2L, 4L, 999L));
            List<Genre> out = repo.findAllByIds(ids);

            assertThat(out).extracting(Genre::getId).containsExactly(2L, 4L, 5L);
            assertThat(out).extracting(Genre::getName).containsExactly("Genre_2", "Genre_4", "Genre_5");
        }

        @Test
        @DisplayName("игнорирует несуществующие id")
        void ignoresUnknownIds() {
            List<Genre> out = repo.findAllByIds(Set.of(1L, 999L, 1000L));

            assertThat(out).hasSize(1);
            assertThat(out.get(0).getId()).isEqualTo(1L);
            assertThat(out.get(0).getName()).isEqualTo("Genre_1");
        }
    }
}
