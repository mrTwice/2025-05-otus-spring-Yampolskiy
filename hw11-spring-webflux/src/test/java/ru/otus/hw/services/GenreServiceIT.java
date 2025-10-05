package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.otus.hw.components.DataSeeder;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.data.mongodb.auto-index-creation=true",
                "spring.data.mongodb.database=library-test-${random.uuid}"
        }
)
@ActiveProfiles("test")
class GenreServiceIT {

    @Autowired private GenreService genreService;

    @Autowired private DataSeeder seeder;

    @Autowired private GenreRepository genreRepository;

    @Autowired private AuthorRepository authorRepository;

    @Autowired private BookRepository bookRepository;

    @Autowired private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        StepVerifier.create(
                Mono.when(
                        commentRepository.deleteAll(),
                        bookRepository.deleteAll(),
                        genreRepository.deleteAll(),
                        authorRepository.deleteAll()
                ).then(seeder.seed())
        ).verifyComplete();
    }

    @Test
    @DisplayName("findAll: возвращает все жанры (отсортированы по name) и совпадают с содержимым БД (сидер)")
    void findAll_returnsSeededGenres() {
        var fromService = genreService.findAll().collectList();
        var inDb       = genreRepository.findAll(Sort.by("name").ascending()).collectList();

        StepVerifier.create(Mono.zip(fromService, inDb))
                .assertNext(t -> {
                    List<Genre> srv = t.getT1();
                    List<Genre> db  = t.getT2();

                    assertThat(srv)
                            .isNotNull()
                            .isNotEmpty()
                            .hasSameSizeAs(db);

                    assertThat(srv)
                            .extracting(Genre::getId)
                            .containsExactlyElementsOf(db.stream().map(Genre::getId).toList());

                    assertThat(srv)
                            .extracting(Genre::getName)
                            .containsExactlyElementsOf(db.stream().map(Genre::getName).toList());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findAll(Pageable): постранично, корректный размер и общее количество")
    void findAll_paged() {
        var page0 = Pageable.ofSize(3).withPage(0);
        var page1 = Pageable.ofSize(3).withPage(1);

        StepVerifier.create(genreService.findAll(page0))
                .assertNext(p -> {
                    assertThat(p.getContent()).hasSize(3);
                    assertThat(p.getTotalElements()).isGreaterThanOrEqualTo(6);
                })
                .verifyComplete();

        StepVerifier.create(genreService.findAll(page1))
                .assertNext(p -> {
                    assertThat(p.getContent()).hasSize(3);
                    assertThat(p.getTotalElements()).isGreaterThanOrEqualTo(6);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("insert: нормализует имя и сохраняет")
    void insert_happyPath() {
        StepVerifier.create(genreService.insert("  New Genre  "))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isNotBlank();
                    assertThat(saved.getName()).isEqualTo("New Genre");
                })
                .verifyComplete();

        StepVerifier.create(genreRepository.findByName("New Genre"))
                .assertNext(found -> assertThat(found.getName()).isEqualTo("New Genre"))
                .verifyComplete();
    }

    @Test
    @DisplayName("insert: пустое/blank имя → ValidationException")
    void insert_blank() {
        StepVerifier.create(genreService.insert("  "))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ru.otus.hw.exceptions.ValidationException.class);
                    assertThat(e.getMessage()).contains("Genre name must not be blank");
                })
                .verify();

        StepVerifier.create(genreService.insert(null))
                .expectError(ru.otus.hw.exceptions.ValidationException.class)
                .verify();
    }
}
