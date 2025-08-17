package ru.otus.hw.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuthorRepository.class)
class AuthorRepositoryTest {

    @Autowired
    TestEntityManager tem;

    @Autowired
    AuthorRepository authorRepository;

    @Test
    void findAll_returnsAllAuthors() {
        var a1 = tem.persistFlushFind(new Author(null, "A1"));
        var a2 = tem.persistFlushFind(new Author(null, "A2"));

        List<Author> all = authorRepository.findAll();

        assertThat(all).extracting(Author::getFullName).contains(a1.getFullName(), a2.getFullName());
    }

    @Test
    void findById_returnsAuthor() {
        var a = tem.persistFlushFind(new Author(null, "A1"));

        var found = authorRepository.findById(a.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("A1");
    }
}