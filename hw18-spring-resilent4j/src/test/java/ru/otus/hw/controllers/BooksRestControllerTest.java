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

import ru.otus.hw.services.BookService;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.dto.*;
import ru.otus.hw.models.*;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BooksRestController.class)
class BooksRestControllerTest extends CommonTest{

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookMapper bookMapper;

    @Test
    void shouldReturnPagedBooks() throws Exception {
        var a = new Author(); a.setId(1L); a.setFullName("Author_1");
        var b1 = new Book(); b1.setId(100L); b1.setTitle("BookTitle_1"); b1.setAuthor(a);
        var b2 = new Book(); b2.setId(101L); b2.setTitle("BookTitle_2"); b2.setAuthor(a);

        var pageReq = PageRequest.of(0, 2);
        var page = new PageImpl<>(List.of(b1, b2), pageReq, 3);

        given(bookService.findAll(pageReq)).willReturn(page);
        given(bookMapper.toListItemDto(b1)).willReturn(
                BookListItemDto.builder().id(100L).title("BookTitle_1")
                        .authorFullName("Author_1").genresSummary("Genre_1, Genre_2").build());
        given(bookMapper.toListItemDto(b2)).willReturn(
                BookListItemDto.builder().id(101L).title("BookTitle_2")
                        .authorFullName("Author_1").genresSummary("Genre_3").build());

        mvc.perform(get("/api/v1/books?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(100)))
                .andExpect(jsonPath("$.content[0].title", is("BookTitle_1")))
                .andExpect(jsonPath("$.content[0].authorFullName", is("Author_1")))
                .andExpect(jsonPath("$.content[0].genresSummary", containsString("Genre_1")))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void shouldReturnBookDetails() throws Exception {
        var aDto = AuthorDto.builder().id(1L).fullName("Author_1").build();
        var g1 = GenreDto.builder().id(10L).name("Genre_1").build();
        var g2 = GenreDto.builder().id(11L).name("Genre_2").build();
        var details = BookDetailsDto.builder()
                .id(100L).title("BookTitle_1").author(aDto).genres(List.of(g1, g2)).version(0L).build();

        given(bookMapper.toDetailsDto(any(Book.class))).willReturn(details);
        given(bookService.findById(100L)).willReturn(new Book());

        mvc.perform(get("/api/v1/books/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.author.fullName", is("Author_1")))
                .andExpect(jsonPath("$.genres", hasSize(2)))
                .andExpect(jsonPath("$.version", is(0)));
    }

    @Test
    void shouldCreateBookAndReturn201WithLocation() throws Exception {
        var form = BookForm.builder()
                .title("New Book")
                .authorId(1L)
                .genresIds(Set.of(10L, 11L))
                .version(0L)
                .build();

        var savedEntity = new Book(); savedEntity.setId(123L);
        given(bookService.insert(eq("New Book"), eq(1L), eq(Set.of(10L, 11L)))).willReturn(savedEntity);

        var dto = BookDetailsDto.builder()
                .id(123L).title("New Book")
                .author(AuthorDto.builder().id(1L).fullName("Author_1").build())
                .genres(List.of(GenreDto.builder().id(10L).name("Genre_1").build()))
                .version(0L).build();
        given(bookMapper.toDetailsDto(savedEntity)).willReturn(dto);

        mvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(form)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/books/123"))
                .andExpect(jsonPath("$.id", is(123)))
                .andExpect(jsonPath("$.title", is("New Book")));
    }

    @Test
    void shouldUpdateBook() throws Exception {
        var form = BookForm.builder()
                .title("Renamed")
                .authorId(2L)
                .genresIds(Set.of(12L))
                .version(5L)
                .build();

        var updated = new Book(); updated.setId(200L);
        given(bookService.update(eq(200L), eq("Renamed"), eq(2L), eq(Set.of(12L)), eq(5L)))
                .willReturn(updated);

        var dto = BookDetailsDto.builder()
                .id(200L).title("Renamed")
                .author(AuthorDto.builder().id(2L).fullName("Author_2").build())
                .genres(List.of(GenreDto.builder().id(12L).name("Genre_12").build()))
                .version(6L).build();
        given(bookMapper.toDetailsDto(updated)).willReturn(dto);

        mvc.perform(put("/api/v1/books/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(200)))
                .andExpect(jsonPath("$.title", is("Renamed")))
                .andExpect(jsonPath("$.author.fullName", is("Author_2")));
    }

    @Test
    void shouldDeleteBook() throws Exception {
        mvc.perform(delete("/api/v1/books/321"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenBookNotFound() throws Exception {
        stubProblemDetailFactory();
        stubStatusMapper();

        var ex = new ru.otus.hw.exceptions.BusinessException(
                ru.otus.hw.exceptions.ErrorCode.NOT_FOUND, "Книга не найдена");
        given(bookService.findById(999L)).willThrow(ex);

        mvc.perform(get("/api/v1/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("NOT_FOUND"));
    }

    @Test
    void shouldReturn400OnInvalidCreateBook() throws Exception {
        stubProblemDetailFactory();

        var bad = BookForm.builder()
                .title("")
                .authorId(null)
                .genresIds(Set.of())
                .build();

        mvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION"));
    }

    @Test
    void shouldReturn409OnDuplicateTitlePerAuthor() throws Exception {
        stubProblemDetailFactory();

        var form = BookForm.builder()
                .title("Same")
                .authorId(1L)
                .genresIds(Set.of(10L))
                .build();

        var hce = new org.hibernate.exception.ConstraintViolationException(
                "UQ_BOOKS_AUTHOR_TITLE", null, "UQ_BOOKS_AUTHOR_TITLE");
        var dive = new org.springframework.dao.DataIntegrityViolationException("dup", hce);
        given(bookService.insert(eq("Same"), eq(1L), eq(Set.of(10L)))).willThrow(dive);

        mvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(form)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("DUPLICATE"))
                .andExpect(jsonPath("$.detail").value("Книга с таким названием у этого автора уже существует"));
    }

    @Test
    void shouldReturn409OnOptimisticLockOnUpdate() throws Exception {
        stubProblemDetailFactory();

        var form = BookForm.builder()
                .title("T")
                .authorId(1L)
                .genresIds(Set.of(10L))
                .version(5L)
                .build();

        var ole = new org.springframework.orm.ObjectOptimisticLockingFailureException(ru.otus.hw.models.Book.class, 200L);
        given(bookService.update(
                eq(200L),
                anyString(),
                anyLong(),
                anySet(),
                anyLong()
        )).willThrow(ole);
        mvc.perform(put("/api/v1/books/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(form)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("CONFLICT"));
    }


}
