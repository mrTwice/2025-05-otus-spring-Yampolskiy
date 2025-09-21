package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import ru.otus.hw.services.GenreService;
import ru.otus.hw.mappers.GenreMapper;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = GenresRestController.class)
class GenresRestControllerTest extends CommonTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private GenreMapper genreMapper;

    @Test
    void shouldReturnPagedGenres() throws Exception {
        var g1 = new Genre(); g1.setId(10L); g1.setName("Genre_1");
        var g2 = new Genre(); g2.setId(11L); g2.setName("Genre_2");

        var pageReq = PageRequest.of(0, 2);
        var page = new PageImpl<>(List.of(g1, g2), pageReq, 6);

        given(genreService.findAll(pageReq)).willReturn(page);
        given(genreMapper.toDto(g1)).willReturn(GenreDto.builder().id(10L).name("Genre_1").build());
        given(genreMapper.toDto(g2)).willReturn(GenreDto.builder().id(11L).name("Genre_2").build());

        mvc.perform(get("/api/v1/genres?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(10)))
                .andExpect(jsonPath("$.content[0].name", is("Genre_1")))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(6)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(false)));
    }


    @Test
    void shouldReturn404ForUnknownEndpoint() throws Exception {
        stubProblemDetailFactory();
        mvc.perform(get("/api/v1/unknown").accept("application/problem+json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("NOT_FOUND"));
    }
}
