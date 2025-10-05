package ru.otus.hw.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.otus.hw.components.DataSeeder;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest(properties = {
        "spring.data.mongodb.auto-index-creation=true",
        "spring.data.mongodb.database=library-test-${random.uuid}"
})
@ActiveProfiles("test")
@Import(DataSeeder.class)
class BookRepositoryIT {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DataSeeder seeder;

    private String author1Id;
    private String author2Id;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll()
                .then(bookRepository.deleteAll())
                .then(genreRepository.deleteAll())
                .then(authorRepository.deleteAll())
                .then(seeder.seed())
                .block();

        Author a1 = authorRepository.findByFullName("Author_1").block();
        Author a2 = authorRepository.findByFullName("Author_2").block();
        assertThat(a1).isNotNull();
        assertThat(a2).isNotNull();
        author1Id = a1.getId();
        author2Id = a2.getId();
    }

    @Test
    @DisplayName("findByTitleAndAuthorId: находит книгу по {title, authorId} из сидера")
    void findByTitleAndAuthorId_found() {
        StepVerifier.create(bookRepository.findByTitleAndAuthorId("BookTitle_1", author1Id))
                .assertNext(b -> assertThat(b.getTitle()).isEqualTo("BookTitle_1"))
                .verifyComplete();

        StepVerifier.create(bookRepository.findByTitleAndAuthorId("BookTitle_1", author2Id))
                .verifyComplete();
    }

    @Test
    @DisplayName("findByAuthorId: возвращает все книги автора")
    void findByAuthorId_returnsBooksOfAuthor() {
        StepVerifier.create(bookRepository.findByAuthorId(author1Id).collectList())
                .assertNext(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).getAuthorId()).isEqualTo(author1Id);
                })
                .verifyComplete();

        StepVerifier.create(bookRepository.findByAuthorId(author2Id).collectList())
                .assertNext(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).getAuthorId()).isEqualTo(author2Id);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("existsByAuthorId: true для автора с книгами и false для автора без книг")
    void existsByAuthorId_trueFalse() {
        StepVerifier.create(bookRepository.existsByAuthorId(author1Id))
                .expectNext(true)
                .verifyComplete();

        String newAuthorId = authorRepository.save(new Author(null, "Author_X", 0L)).map(Author::getId).block();
        StepVerifier.create(bookRepository.existsByAuthorId(newAuthorId))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("unique {title, authorId}: попытка вставить дубликат пары бросает DuplicateKeyException")
    void unique_titleAuthor_duplicate() {
        String anyGenreId = genreRepository.findAll()
                .map(Genre::getId)
                .next()
                .block();

        Book dup = new Book();
        dup.setTitle("BookTitle_1");
        dup.setAuthorId(author1Id);
        dup.setGenresIds(List.of(anyGenreId));
        dup.setVersion(0L);

        StepVerifier.create(bookRepository.save(dup))
                .expectError(DuplicateKeyException.class)
                .verify();
    }
}
