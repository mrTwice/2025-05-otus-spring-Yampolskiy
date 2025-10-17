package ru.otus.hw.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.library.models.Author;
import ru.otus.hw.library.models.Book;
import ru.otus.hw.library.models.Comment;
import ru.otus.hw.library.models.Genre;
import ru.otus.hw.library.repositories.CommentRepository;
import ru.otus.hw.users.model.User;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    TestEntityManager tem;

    @Autowired
    CommentRepository commentRepository;

    @Test
    void findByBookId_returnsLatestFirst_andBookIsAccessible() {

        var user = User.builder()
                .username("u1")
                .email("u1@example.com")
                .passwordHash("hash")
                .enabled(true)
                .roles(Set.of("ROLE_USER"))
                .build();
        user = tem.persistFlushFind(user);

        var a = tem.persistFlushFind(new Author("A"));
        var g = tem.persistFlushFind(new Genre("G"));

        var b = new Book();
        b.setTitle("T");
        b.setAuthor(a);
        b.setGenres(Set.of(g));
        b = tem.persistFlushFind(b);

        var c1 = new Comment("first", b, user);
        tem.persist(c1);

        var c2 = new Comment();
        c2.setText("second");
        c2.setBook(b);
        c2.setAuthor(user);
        tem.persist(c2);

        tem.flush();
        tem.clear();

        var list = commentRepository.findByBookIdOrderByCreatedAtDesc(b.getId());
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getText()).isEqualTo("second");
        assertThat(list.get(1).getText()).isEqualTo("first");
        assertThat(list.get(0).getBook().getId()).isEqualTo(b.getId());
    }

}
