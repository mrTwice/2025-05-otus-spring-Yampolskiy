package ru.otus.hw.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaBookRepository.class)
class BookRepositoryTest {

    @Autowired
    TestEntityManager tem;

    @Autowired
    BookRepository bookRepository;

    @Test
    void findAll_fetchesAuthorAndGenres_withoutLazyErrorsInsideTestTx() {
        var a = tem.persistFlushFind(new Author(null, "AuthorA"));
        var g1 = tem.persistFlushFind(new Genre(null, "G1"));
        var g2 = tem.persistFlushFind(new Genre(null, "G2"));

        var b = new Book();
        b.setTitle("T");
        b.setAuthor(a);
        b.setGenres(List.of(g1, g2));
        tem.persist(b);
        tem.flush();
        tem.clear();

        var all = bookRepository.findAll();

        var loaded = all.stream()
                .filter(x -> "T".equals(x.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Created book not found in result"));

        assertThat(loaded.getAuthor().getFullName()).isEqualTo("AuthorA");
        assertThat(loaded.getGenres()).extracting(Genre::getName)
                .containsExactlyInAnyOrder("G1", "G2");
    }

    @Test
    void findById_fetchesAuthorAndGenres() {
        var a = tem.persistFlushFind(new Author(null, "AuthorA"));
        var g1 = tem.persistFlushFind(new Genre(null, "G1"));
        var b = new Book();
        b.setTitle("T2");
        b.setAuthor(a);
        b.setGenres(List.of(g1));
        tem.persist(b);
        tem.flush();
        tem.clear();

        var opt = bookRepository.findById(b.getId());
        assertThat(opt).isPresent();
        var loaded = opt.get();
        assertThat(loaded.getAuthor().getFullName()).isEqualTo("AuthorA");
        assertThat(loaded.getGenres()).extracting(Genre::getName).containsExactly("G1");
    }
}
