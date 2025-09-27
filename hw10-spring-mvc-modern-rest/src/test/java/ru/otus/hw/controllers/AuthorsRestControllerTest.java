package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import ru.otus.hw.services.AuthorService;
import ru.otus.hw.mappers.AuthorMapper;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AuthorsRestController.class)
class AuthorsRestControllerTest extends CommonTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private AuthorMapper authorMapper;

    @Test
    void shouldReturnAuthorsList() throws Exception {
        var a1 = new Author(); a1.setId(1L); a1.setFullName("Author_1");
        var a2 = new Author(); a2.setId(2L); a2.setFullName("Author_2");
        var authors = List.of(a1, a2);

        given(authorService.findAll()).willReturn(authors);
        given(authorMapper.toDto(a1)).willReturn(AuthorDto.builder().id(1L).fullName("Author_1").build());
        given(authorMapper.toDto(a2)).willReturn(AuthorDto.builder().id(2L).fullName("Author_2").build());

        mvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fullName", is("Author_1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].fullName", is("Author_2")));

        Mockito.verify(authorService).findAll();
    }

    @Test
    void shouldReturn404ForUnknownEndpoint() throws Exception {
        stubProblemDetailFactory();

        mvc.perform(get("/api/v1/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("NOT_FOUND"));
    }
}
