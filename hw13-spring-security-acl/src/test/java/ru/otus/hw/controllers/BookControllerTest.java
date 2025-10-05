package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.library.components.GlobalExceptionHandler;
import ru.otus.hw.library.controllers.BookController;
import ru.otus.hw.library.dto.*;
import ru.otus.hw.library.mappers.BookMapper;
import ru.otus.hw.library.mappers.CommentMapper;
import ru.otus.hw.library.models.Author;
import ru.otus.hw.library.models.Book;
import ru.otus.hw.library.models.Comment;
import ru.otus.hw.library.models.Genre;
import ru.otus.hw.library.services.AuthorService;
import ru.otus.hw.library.services.BookService;
import ru.otus.hw.library.services.CommentService;
import ru.otus.hw.library.services.GenreService;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BookController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class
        )
)
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private BookMapper bookMapper;

    @MockitoBean
    private CommentMapper commentMapper;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void shouldReturnBooksListView() throws Exception {
        // entity
        var author = new Author("Author1");
        var book = new Book(1L, "Book1", author, Set.of(), 0L);
        Mockito.when(bookService.findAll()).thenReturn(List.of(book));

        var listItemDto = Mockito.mock(BookListItemDto.class);
        Mockito.when(bookMapper.toListItemDto(book)).thenReturn(listItemDto);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/list"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("books", hasSize(1)));
    }

    @Test
    @WithMockUser
    void shouldReturnBookDetails() throws Exception {
        var author = new Author("Author1");
        var genre = new Genre("Drama");
        var book = new Book(1L, "Book1", author, Set.of(genre), 0L);

        Mockito.when(bookService.findById(1L)).thenReturn(book);

        var authorDto = AuthorDto.builder()
                .id(1L)
                .fullName("Author1")
                .build();

        var genreDto = GenreDto.builder()
                .id(10L)
                .name("Drama")
                .build();

        var detailsDto = BookDetailsDto.builder()
                .id(1L)
                .title("Book1")
                .author(authorDto)
                .genres(List.of(genreDto))
                .version(0L)
                .build();

        Mockito.when(bookMapper.toDetailsDto(book)).thenReturn(detailsDto);

        var commentEntity = new Comment("Nice", book); // если у тебя есть такой конструктор
        Mockito.when(commentService.findByBookId(1L)).thenReturn(List.of(commentEntity));

        var commentDto = CommentDto.builder()
                .id(100L)
                .text("Nice")
                .createdAt(Instant.now())
                .bookId(1L)
                .build();
        Mockito.when(commentMapper.toDto(commentEntity)).thenReturn(commentDto);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/details"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attributeExists("commentForm"));
    }

    @Test
    @WithMockUser
    void shouldShowCreateForm() throws Exception {
        Mockito.when(authorService.findAll()).thenReturn(List.of(new Author("Author1")));
        Mockito.when(genreService.findAll()).thenReturn(List.of(new Genre("Drama")));

        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attributeExists("genres"));
    }

    @Test
    @WithMockUser
    void shouldCreateBook() throws Exception {
        Book saved = new Book(10L, "New Book", new Author("Author1"), Set.of(), 0);
        Mockito.when(bookService.insert(eq("New Book"), eq(1L), any())).thenReturn(saved);

        mockMvc.perform(post("/books")
                        .param("title", "New Book")
                        .param("authorId", "1")
                        .param("genresIds", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/10"));
    }

    @Test
    @WithMockUser
    void shouldUpdateBook() throws Exception {
        Book updated = new Book(10L, "Updated", new Author("Author1"), Set.of(), 0);
        Mockito.when(bookService.update(eq(10L), eq("Updated"), eq(1L), any())).thenReturn(updated);

        mockMvc.perform(post("/books/10")
                        .param("title", "Updated")
                        .param("authorId", "1")
                        .param("genresIds", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/10"));
    }

    @Test
    @WithMockUser
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(post("/books/10/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        Mockito.verify(bookService).deleteById(10L);
    }
}
