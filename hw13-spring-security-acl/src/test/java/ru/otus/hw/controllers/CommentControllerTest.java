package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.library.components.GlobalExceptionHandler;
import ru.otus.hw.library.controllers.CommentController;
import ru.otus.hw.library.dto.AuthorDto;
import ru.otus.hw.library.dto.BookDetailsDto;
import ru.otus.hw.library.dto.CommentDto;
import ru.otus.hw.library.dto.GenreDto;
import ru.otus.hw.library.mappers.BookMapper;
import ru.otus.hw.library.mappers.CommentMapper;
import ru.otus.hw.library.models.Comment;
import ru.otus.hw.library.models.Author;
import ru.otus.hw.library.models.Book;
import ru.otus.hw.library.models.Genre;
import ru.otus.hw.library.services.BookService;
import ru.otus.hw.library.services.CommentService;
import ru.otus.hw.security.config.SecurityConfig;
import ru.otus.hw.security.model.AppUserDetails;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CommentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class
        )
)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookMapper bookMapper;

    @MockitoBean
    private CommentMapper commentMapper;

    @MockitoBean
    private UserDetailsService userDetailsService;


    @Test
    void shouldCreateCommentAndRedirectToBookDetails() throws Exception {
        AppUserDetails me = new AppUserDetails(
                2L,
                "user",
                "password",
                true,
                Set.of("ROLE_USER")
        );

        mockMvc.perform(post("/books/{bookId}/comments", 1L)
                        .param("text", "Great book!")
                        .with(user(me))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(commentService).insert(1L, 2L, "Great book!");

    }

    @Test
    @WithMockUser
    void shouldReturnDetailsViewOnValidationErrors() throws Exception {
        long bookId = 1L;

        var author = new Author("Author1");
        var genre = new Genre("Drama");
        var book = new Book(bookId, "Book1", author, Set.of(genre), 0L);

        when(bookService.findById(bookId)).thenReturn(book);

        var authorDto = AuthorDto.builder().id(1L).fullName("Author1").build();
        var genreDto = GenreDto.builder().id(10L).name("Drama").build();
        var bookDetailsDto = BookDetailsDto.builder()
                .id(bookId).title("Book1").author(authorDto)
                .genres(List.of(genreDto)).version(0L)
                .build();
        when(bookMapper.toDetailsDto(book)).thenReturn(bookDetailsDto);

        var commentDto = CommentDto.builder()
                .id(100L).text("Nice").createdAt(Instant.now()).bookId(bookId).build();
        when(commentService.findByBookId(bookId)).thenReturn(List.of(
                new Comment("Nice", book)
        ));
        when(commentMapper.toDto(Mockito.any(Comment.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/books/{bookId}/comments", bookId)
                        .param("text", "")
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("book/details"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attributeExists("commentForm"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteCommentAndRedirectToBookDetails() throws Exception {
        long bookId = 1L;
        long commentId = 77L;

        mockMvc.perform(
                post("/books/{bookId}/comments/{commentId}/delete", bookId, commentId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + bookId));

        verify(commentService).deleteById(commentId);
    }
}
