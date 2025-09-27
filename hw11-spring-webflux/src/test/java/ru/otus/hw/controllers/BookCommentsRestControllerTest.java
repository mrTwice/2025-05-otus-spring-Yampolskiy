package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.otus.hw.components.ErrorCodeHttpStatusMapper;
import ru.otus.hw.components.GlobalRestExceptionHandler;
import ru.otus.hw.components.ProblemDetailFactory;
import ru.otus.hw.components.ValidationErrorExtractor;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.BusinessException;
import ru.otus.hw.exceptions.ErrorCode;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.CommentService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = BookCommentsRestController.class)
@Import({GlobalRestExceptionHandler.class})
class BookCommentsRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private CommentMapper commentMapper;

    @MockitoBean
    private ErrorCodeHttpStatusMapper statusMapper;

    @MockitoBean
    private ProblemDetailFactory pdf;

    @MockitoBean
    private ValidationErrorExtractor valExtractor;

    @BeforeEach
    void setupPdfMocks() {
        when(pdf.create(any(HttpStatus.class), anyString(), anyString(), anyMap()))
                .thenAnswer(inv -> {
                    HttpStatus status = inv.getArgument(0);
                    String title = inv.getArgument(1);
                    String detail = inv.getArgument(2);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> props = inv.getArgument(3);
                    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
                    if (title != null) pd.setTitle(title);
                    if (props != null) props.forEach(pd::setProperty);
                    return pd;
                });

        when(pdf.create(any(HttpStatus.class), anyString(), anyString()))
                .thenAnswer(inv -> {
                    HttpStatus status = inv.getArgument(0);
                    String title = inv.getArgument(1);
                    String detail = inv.getArgument(2);
                    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
                    if (title != null) pd.setTitle(title);
                    return pd;
                });
    }

    private CommentDto dto(String id, String text) {
        return CommentDto.builder().id(id).text(text).build();
    }

    @Test
    @DisplayName("GET /api/v1/books/{bookId}/comments -> возвращает PageResponse<CommentDto>")
    void list_ok() {
        String bookId = "b1";
        var pageable = PageRequest.of(0, 2);

        var c1 = Mockito.mock(Comment.class);
        var c2 = Mockito.mock(Comment.class);

        Page<Comment> page =
                new PageImpl<>(List.of(c1, c2), pageable, 5);

        when(commentService.findByBookId(bookId, pageable)).thenReturn(Mono.just(page));

        var d1 = dto("c1", "Nice!");
        var d2 = dto("c2", "Great book");
        when(commentMapper.toDto(same(c1))).thenReturn(d1);
        when(commentMapper.toDto(same(c2))).thenReturn(d2);

        webTestClient.get()
                .uri("/api/v1/books/{bookId}/comments?page=0&size=2", bookId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].id").isEqualTo("c1")
                .jsonPath("$.content[0].text").isEqualTo("Nice!")
                .jsonPath("$.page").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(5);

        verify(commentService).findByBookId(bookId, pageable);
        verify(commentMapper).toDto(same(c1));
        verify(commentMapper).toDto(same(c2));
    }

    @Test
    @DisplayName("GET /api/v1/books/{bookId}/comments -> 400, если плохие параметры (обработано advice.handleInput)")
    void list_badInput_400() {
        String bookId = "b1";
        when(commentService.findByBookId(eq(bookId), any(Pageable.class)))
                .thenReturn(Mono.error(new ServerWebInputException("page index must not be less than zero")));

        webTestClient.get()
                .uri("/api/v1/books/{bookId}/comments?page=-1&size=0", bookId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("VALIDATION")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("page index must not be less than zero")
                .jsonPath("$.code").isEqualTo("VALIDATION");
    }

    @Test
    @DisplayName("POST /api/v1/books/{bookId}/comments -> 201 + Location + тело CommentDto")
    void create_ok() {
        String bookId = "b1";

        var saved = new Comment("c1", "Nice!", Instant.now(),bookId, 0L);

        when(commentService.insert(bookId, "Nice!")).thenReturn(Mono.just(saved));
        when(commentMapper.toDto(saved)).thenReturn(CommentDto.builder()
                .id("c1").text("Nice!").bookId(bookId).createdAt(Instant.now()).build());


        var dto = dto("c1", "Nice!");
        when(commentMapper.toDto(same(saved))).thenReturn(dto);

        webTestClient.post()
                .uri("/api/v1/books/{bookId}/comments", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"text":"Nice!"}
                """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/v1/books/b1/comments/c1")
                .expectBody()
                .jsonPath("$.id").isEqualTo("c1")
                .jsonPath("$.text").isEqualTo("Nice!");

        verify(commentService).insert(bookId, "Nice!");
    }

    @Test
    @DisplayName("POST /api/v1/books/{bookId}/comments -> 400 при валидации тела (WebExchangeBindException)")
    void create_validation_400() {
        when(valExtractor.toList(any(BindingResult.class)))
                .thenReturn(List.of(Map.of("field", "text", "message", "must not be blank")));

        webTestClient.post()
                .uri("/api/v1/books/{bookId}/comments", "b1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"text":""}
                """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("VALIDATION")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("Validation failed")
                .jsonPath("$.code").isEqualTo("VALIDATION")
                .jsonPath("$.errors.length()").isEqualTo(1)
                .jsonPath("$.errors[0].field").isEqualTo("text");

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("POST /api/v1/books/{bookId}/comments -> 404 если книга не найдена (BusinessException NOT_FOUND)")
    void create_businessNotFound_404() {
        String bookId = "missing";
        when(commentService.insert(bookId, "Hi"))
                .thenReturn(Mono.error(new BusinessException(ErrorCode.NOT_FOUND, "Книга не найдена")));
        when(statusMapper.toStatus(ErrorCode.NOT_FOUND)).thenReturn(HttpStatus.NOT_FOUND);

        webTestClient.post()
                .uri("/api/v1/books/{bookId}/comments", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"text":"Hi"}
                """)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("NOT_FOUND")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("Книга не найдена")
                .jsonPath("$.code").isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{bookId}/comments/{commentId} -> 204 No Content")
    void delete_ok() {
        when(commentService.deleteById("c1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/books/{bookId}/comments/{commentId}", "b1", "c1")
                .exchange()
                .expectStatus().isNoContent();

        verify(commentService).deleteById("c1");
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{bookId}/comments/{commentId} -> 404 (ResponseStatusException)")
    void delete_notFound_404() {
        when(commentService.deleteById("missing"))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "comment not found")));

        webTestClient.delete()
                .uri("/api/v1/books/{bookId}/comments/{commentId}", "b1", "missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("NOT_FOUND")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("comment not found")
                .jsonPath("$.code").isEqualTo("NOT_FOUND");
    }
}
