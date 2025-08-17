package ru.otus.hw.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaCommentRepository.class)
class CommentRepositoryTest {

    @Autowired
    TestEntityManager tem;

    @Autowired
    CommentRepository commentRepository;

    @Test
    void findByBookId_returnsLatestFirst_andBookIsAccessible() {
        var a = tem.persistFlushFind(new Author(null, "A"));
        var g = tem.persistFlushFind(new Genre(null, "G"));
        var b = new Book();
        b.setTitle("T");
        b.setAuthor(a);
        b.setGenres(List.of(g));
        tem.persist(b);

        var c1 = new Comment();
        c1.setText("first");
        c1.setBook(b);
        tem.persist(c1);

        var c2 = new Comment();
        c2.setText("second");
        c2.setBook(b);
        tem.persist(c2);

        tem.flush();
        tem.clear();

        var list = commentRepository.findByBookId(b.getId());
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getText()).isEqualTo("second");
        assertThat(list.get(1).getText()).isEqualTo("first");

        assertThat(list.get(0).getBook().getId()).isEqualTo(b.getId());
    }
}
