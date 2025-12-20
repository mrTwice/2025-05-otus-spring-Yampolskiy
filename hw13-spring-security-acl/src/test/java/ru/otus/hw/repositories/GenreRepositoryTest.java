package ru.otus.hw.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.library.models.Genre;
import ru.otus.hw.library.repositories.GenreRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GenreRepositoryTest {

    @Autowired
    TestEntityManager tem;

    @Autowired
    GenreRepository genreRepository;

    @Test
    void findAll_returnsAllGenres() {
        var g1 = tem.persistFlushFind(new Genre("G1"));
        var g2 = tem.persistFlushFind(new Genre("G2"));

        var all = genreRepository.findAll();
        assertThat(all).extracting(Genre::getName).contains("G1", "G2");
    }

    @Test
    void findAllByIds_returnsSubset() {
        var g1 = tem.persistFlushFind(new Genre("G1"));
        var g2 = tem.persistFlushFind(new Genre( "G2"));
        tem.flush();

        var subset = genreRepository.findByIdIn(Set.of(g1.getId()));
        assertThat(subset).hasSize(1);
        assertThat(subset.get(0).getId()).isEqualTo(g1.getId());
    }
}
