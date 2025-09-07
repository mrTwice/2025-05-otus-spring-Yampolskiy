package ru.otus.hw.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.config.DataSeeder;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
@EnableMongoRepositories(basePackageClasses = CommentRepository.class)
@EnableMongoAuditing
@Import(DataSeeder.class)
class CommentRepositoryIT {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private DataSeeder seeder;

    private String book1Id;
    private String book2Id;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        seeder.seed();

        book1Id = bookRepository.findAll().stream()
                .filter(b -> "BookTitle_1".equals(b.getTitle()))
                .findFirst().orElseThrow().getId();
        book2Id = bookRepository.findAll().stream()
                .filter(b -> "BookTitle_2".equals(b.getTitle()))
                .findFirst().orElseThrow().getId();
    }

    @Test
    @DisplayName("findByBookIdOrderByCreatedAtDesc: сортирует по createdAt по убыванию")
    void findByBookId_sorted_desc() {
        commentRepository.save(new Comment(null, "t1", Instant.now().minusSeconds(3), book1Id));
        commentRepository.save(new Comment(null, "t2", Instant.now().minusSeconds(2), book1Id));
        commentRepository.save(new Comment(null, "t3", Instant.now().minusSeconds(1), book1Id));

        var result = commentRepository.findByBookIdOrderByCreatedAtDesc(book1Id);

        var onlyAdded = result.stream()
                .filter(c -> List.of("t3", "t2", "t1").contains(c.getText()))
                .map(Comment::getText)
                .toList();

        assertThat(onlyAdded).containsExactly("t3", "t2", "t1");
    }

    @Test
    @DisplayName("findByBookIdOrderByCreatedAtDesc: для неизвестной книги возвращает пусто")
    void findByBookId_unknown_returnsEmpty() {
        assertThat(commentRepository.findByBookIdOrderByCreatedAtDesc("missing")).isEmpty();
    }

    @Test
    @DisplayName("deleteByBookId: удаляет только комментарии указанной книги и возвращает их количество")
    void deleteByBookId_deletesOnlyTargetBooksComments() {
        var c1 = new Comment(null, "a1", Instant.now().minusSeconds(3), book1Id);
        var c2 = new Comment(null, "a2", Instant.now().minusSeconds(2), book1Id);
        var c3 = new Comment(null, "b1", Instant.now().minusSeconds(1), book2Id);
        commentRepository.saveAll(List.of(c1, c2, c3));

        int beforeBook1 = commentRepository.findByBookIdOrderByCreatedAtDesc(book1Id).size();
        int beforeBook2 = commentRepository.findByBookIdOrderByCreatedAtDesc(book2Id).size();

        long deleted = commentRepository.deleteByBookId(book1Id);

        assertThat(deleted).isEqualTo(beforeBook1);
        assertThat(commentRepository.findByBookIdOrderByCreatedAtDesc(book1Id)).isEmpty();
        assertThat(commentRepository.findByBookIdOrderByCreatedAtDesc(book2Id)).hasSize(beforeBook2);
    }
}
