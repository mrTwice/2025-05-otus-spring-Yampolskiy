package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import ru.otus.hw.services.CommentService;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentForm;
import ru.otus.hw.models.Comment;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(controllers = BookCommentsRestController.class)
class BookCommentsRestControllerTest extends CommonTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private CommentMapper commentMapper;

    @Test
    void shouldReturnPagedCommentsForBook() throws Exception {
        long bookId = 100L;

        var c1 = new Comment(); c1.setId(1L);
        var c2 = new Comment(); c2.setId(2L);

        var pageReq = PageRequest.of(0, 2);
        var page = new PageImpl<>(List.of(c1, c2), pageReq, 5);

        given(commentService.findByBookId(eq(bookId), eq(pageReq))).willReturn(page);
        given(commentMapper.toDto(c1)).willReturn(CommentDto.builder()
                .id(1L).text("Great book!").createdAt(Instant.parse("2024-01-01T00:00:00Z")).bookId(bookId).build());
        given(commentMapper.toDto(c2)).willReturn(CommentDto.builder()
                .id(2L).text("Not my cup of tea").createdAt(Instant.parse("2024-01-02T00:00:00Z")).bookId(bookId).build());

        mvc.perform(get("/api/v1/books/{bookId}/comments?page=0&size=2", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].bookId", is((int) bookId)))
                .andExpect(jsonPath("$.totalElements", is(5)));
    }

    @Test
    void shouldCreateCommentAndReturn201WithLocation() throws Exception {
        long bookId = 100L;
        var form = CommentForm.builder().text("Awesome read").build();

        var savedEntity = new Comment(); savedEntity.setId(77L);
        given(commentService.insert(eq(bookId), eq("Awesome read"))).willReturn(savedEntity);

        var dto = CommentDto.builder()
                .id(77L).text("Awesome read").bookId(bookId).createdAt(Instant.parse("2024-01-03T00:00:00Z"))
                .build();
        given(commentMapper.toDto(savedEntity)).willReturn(dto);

        mvc.perform(post("/api/v1/books/{bookId}/comments", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(form)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/books/100/comments/77"))
                .andExpect(jsonPath("$.id", is(77)))
                .andExpect(jsonPath("$.text", is("Awesome read")));
    }

    @Test
    void shouldDeleteComment() throws Exception {
        mvc.perform(delete("/api/v1/books/{bookId}/comments/{commentId}", 100L, 77L))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn400OnEmptyCommentText() throws Exception {
        stubProblemDetailFactory();

        var form = CommentForm.builder().text("").build();
        mvc.perform(post("/api/v1/books/{bookId}/comments", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(form)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION"));
    }

    @Test
    void shouldReturn404WhenDeletingMissingComment() throws Exception {
        stubProblemDetailFactory();
        stubStatusMapper();

        var ex = new ru.otus.hw.exceptions.BusinessException(
                ru.otus.hw.exceptions.ErrorCode.NOT_FOUND, "Комментарий не найден");
        doThrow(ex).when(commentService).deleteById(777L);

        mvc.perform(delete("/api/v1/books/{bookId}/comments/{id}", 100L, 777L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("NOT_FOUND"));
    }
}
