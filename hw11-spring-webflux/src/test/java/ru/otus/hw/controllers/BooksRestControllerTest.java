package ru.otus.hw.controllers;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.otus.hw.components.ErrorCodeHttpStatusMapper;
import ru.otus.hw.components.GlobalRestExceptionHandler;
import ru.otus.hw.components.ProblemDetailFactory;
import ru.otus.hw.components.ValidationErrorExtractor;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDetailsDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.BusinessException;
import ru.otus.hw.exceptions.ErrorCode;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


import org.junit.jupiter.api.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

@WebFluxTest(controllers = BooksRestController.class)
@Import({GlobalRestExceptionHandler.class})
class BooksRestControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    BookService bookService;

    @MockitoBean
    AuthorService authorService;

    @MockitoBean
    GenreService genreService;

    @MockitoBean
    BookMapper bookMapper;

    @MockitoBean
    ErrorCodeHttpStatusMapper statusMapper;

    @MockitoBean
    ProblemDetailFactory pdf;

    @MockitoBean
    ValidationErrorExtractor valExtractor;

    private Book book(String id, String title, String authorId, List<String> genreIds, long version) {
        return new Book(id, title, authorId, genreIds, version);
    }
    private Author author(String id, String fullName) {
        return new Author(id, fullName, 0L);
    }

    private Genre genre(String id, String name) {
        return new Genre(id, name, 0L);
    }

    private AuthorDto dtoAuthor(String id, String fullName) {
        return AuthorDto.builder().id(id).fullName(fullName).build();
    }

    private GenreDto dtoGenre(String id, String name) {
        return GenreDto.builder().id(id).name(name).build();
    }

    private ProblemDetail pd(HttpStatus status, String title, String detail, Map<String, Object> props) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        if (title != null) pd.setTitle(title);
        if (props != null) props.forEach(pd::setProperty);
        return pd;
    }

    @BeforeEach
    void defaultPdfMock() {
        when(pdf.create(any(HttpStatus.class), anyString(), anyString(), anyMap()))
                .thenAnswer(inv -> pd(
                        inv.getArgument(0),
                        inv.getArgument(1),
                        inv.getArgument(2),
                        inv.getArgument(3)
                ));
        when(pdf.create(any(HttpStatus.class), anyString(), anyString()))
                .thenAnswer(inv -> pd(
                        inv.getArgument(0),
                        inv.getArgument(1),
                        inv.getArgument(2),
                        Map.of()
                ));
    }

    @Test
    void details_ok() {
        var b = book("b1","Clean Code","a1", List.of("g1","g2"), 1);
        var a = author("a1","Robert C. Martin");
        var genres = List.of(genre("g1","Programming"), genre("g2","Design"));

        when(bookService.findById("b1")).thenReturn(Mono.just(b));
        when(authorService.findById("a1")).thenReturn(Mono.just(a));
        when(genreService.findByIds(new LinkedHashSet<>(List.of("g1","g2"))))
                .thenReturn(Mono.just(genres));

        var dto = BookDetailsDto.builder()
                .id("b1")
                .title("Clean Code")
                .author(dtoAuthor("a1", "Robert C. Martin"))
                .genres(List.of(dtoGenre("g1","Programming"), dtoGenre("g2","Design")))
                .version(1)
                .build();

        when(bookMapper.toDetailsDto(b, a, genres)).thenReturn(dto);

        webTestClient.get()
                .uri("/api/v1/books/{id}", "b1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("b1")
                .jsonPath("$.title").isEqualTo("Clean Code")
                .jsonPath("$.author.id").isEqualTo("a1")
                .jsonPath("$.author.fullName").isEqualTo("Robert C. Martin")
                .jsonPath("$.genres.length()").isEqualTo(2)
                .jsonPath("$.genres[0].id").isEqualTo("g1")
                .jsonPath("$.genres[0].name").isEqualTo("Programming")
                .jsonPath("$.genres[1].name").isEqualTo("Design")
                .jsonPath("$.version").isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/v1/books -> 201 Created + Location + тело с BookDetailsDto (nested DTOs)")
    void create_shouldReturn201AndLocationAndBody() {
        var saved = book("b1", "Clean Code", "a1", List.of("g1","g2"), 0);
        var a = author("a1", "Robert C. Martin");
        var genres = List.of(genre("g1","Programming"), genre("g2","Design"));

        var dto = BookDetailsDto.builder()
                .id("b1").title("Clean Code")
                .author(dtoAuthor("a1","Robert C. Martin"))
                .genres(List.of(dtoGenre("g1","Programming"), dtoGenre("g2","Design")))
                .version(0)
                .build();

        when(bookService.insert("Clean Code", "a1", Set.of("g1","g2"))).thenReturn(Mono.just(saved));
        when(authorService.findById("a1")).thenReturn(Mono.just(a));
        when(genreService.findByIds(new LinkedHashSet<>(List.of("g1","g2"))))
                .thenReturn(Mono.just(genres));
        when(bookMapper.toDetailsDto(saved, a, genres)).thenReturn(dto);

        webTestClient.post()
                .uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                {"title":"Clean Code","authorId":"a1","genresIds":["g1","g2"]}
            """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/v1/books/b1")
                .expectBody()
                .jsonPath("$.id").isEqualTo("b1")
                .jsonPath("$.author.fullName").isEqualTo("Robert C. Martin")
                .jsonPath("$.genres[0].name").isEqualTo("Programming")
                .jsonPath("$.version").isEqualTo(0);
    }


    @Test
    void details_businessNotFound_loggedWarn_and_404_with_problem_detail() {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalRestExceptionHandler.class);
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            var ex = new BusinessException(ErrorCode.NOT_FOUND, "Книга не найдена");
            when(bookService.findById("missing")).thenReturn(Mono.error(ex));

            when(statusMapper.toStatus(ErrorCode.NOT_FOUND)).thenReturn(HttpStatus.NOT_FOUND);

            webTestClient.get()
                    .uri("/api/v1/books/{id}", "missing")
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.title").isEqualTo("NOT_FOUND")
                    .jsonPath("$.status").isEqualTo(404)
                    .jsonPath("$.detail").isEqualTo("Книга не найдена")
                    .jsonPath("$.code").isEqualTo("NOT_FOUND");

            var events = appender.list;
            boolean hasWarn = events.stream().anyMatch(e ->
                    e.getLevel() == Level.WARN &&
                            e.getFormattedMessage().contains("NOT_FOUND: Книга не найдена")
            );
            assertTrue(hasWarn, "Ожидали WARN лог с кодом и сообщением");
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(oldLevel);
        }
    }

    @Test
    void create_businessBadRequest_loggedDebug_and_400() {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalRestExceptionHandler.class);
        Level old = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        ListAppender<ILoggingEvent> app = new ListAppender<>();
        app.start();
        logger.addAppender(app);

        try {
            String body = """
            {"title":"Clean Code","authorId":"a1","genresIds":["g1"]}
        """;

            when(bookService.insert("Clean Code", "a1", Set.of("g1")))
                    .thenReturn(Mono.error(new BusinessException(ErrorCode.VALIDATION, "Неверные данные")));

            when(statusMapper.toStatus(ErrorCode.VALIDATION)).thenReturn(HttpStatus.BAD_REQUEST);

            webTestClient.post()
                    .uri("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.title").isEqualTo("VALIDATION")
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.detail").isEqualTo("Неверные данные")
                    .jsonPath("$.code").isEqualTo("VALIDATION");


            boolean hasDebug = app.list.stream().anyMatch(e ->
                    e.getLevel() == Level.DEBUG &&
                            e.getFormattedMessage().contains("VALIDATION: Неверные данные")
            );
            assertTrue(hasDebug, "Ожидали DEBUG лог для бизнес-валидации");
        } finally {
            logger.detachAppender(app);
            logger.setLevel(old);
        }
    }



    @Test
    void create_validationBinding_400_problem_with_errors() {
        when(valExtractor.toList(any(BindingResult.class)))
                .thenReturn(List.of(
                        Map.of("field", "title", "message", "must not be blank"),
                        Map.of("field", "authorId", "message", "must not be null")
                ));
        webTestClient.post()
                .uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"title":"", "genresIds":["g1","g2"]}
                    """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("VALIDATION")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("Validation failed")
                .jsonPath("$.code").isEqualTo("VALIDATION")
                .jsonPath("$.errors.length()").isEqualTo(2)
                .jsonPath("$.errors[0].field").isEqualTo("title");
        verifyNoInteractions(bookService);
    }


    @Test
    void create_duplicateKey_conflict_with_specific_message() {
        when(bookService.insert(anyString(), anyString(), anySet()))
                .thenReturn(Mono.error(new DuplicateKeyException("violates unique constraint UQ_BOOKS_AUTHOR_TITLE")));

        webTestClient.post()
                .uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"title":"Clean Code","authorId":"a1","genresIds":["g1"]}
                    """)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.title").isEqualTo("DUPLICATE")
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.detail").isEqualTo("Книга с таким названием у этого автора уже существует")
                .jsonPath("$.code").isEqualTo("DUPLICATE");
    }


    @Test
    void update_optimisticLocking_conflict() {
        when(bookService.update(eq("b1"), anyString(), anyString(), anySet(), anyLong()))
                .thenReturn(Mono.error(new OptimisticLockingFailureException("version mismatch")));

        webTestClient.put()
                .uri("/api/v1/books/{id}", "b1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"title":"X","authorId":"a1","genresIds":["g1"], "version":3}
                    """)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.title").isEqualTo("CONFLICT")
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.detail").isEqualTo("Конфликт версий (объект был изменён конкурентно)")
                .jsonPath("$.code").isEqualTo("CONFLICT");
    }


    @Test
    void details_responseStatus_notFound() {
        when(bookService.findById("x"))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "не найдено")));

        webTestClient.get()
                .uri("/api/v1/books/{id}", "x")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("не найдено")
                .jsonPath("$.title").isEqualTo("NOT_FOUND")
                .jsonPath("$.code").isEqualTo("NOT_FOUND");
    }


    @Test
    void list_serverWebInput_badRequest() {
        when(bookService.findAll(any(Pageable.class)))
                .thenReturn(Mono.error(new ServerWebInputException("page index must not be less than zero")));

        webTestClient.get()
                .uri("/api/v1/books?page=-1&size=0")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("VALIDATION")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").exists();
    }
}

