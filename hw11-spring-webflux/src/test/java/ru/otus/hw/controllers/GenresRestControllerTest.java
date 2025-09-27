package ru.otus.hw.controllers;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.otus.hw.components.GlobalRestExceptionHandler;
import ru.otus.hw.dto.GenreForm;
import ru.otus.hw.mappers.GenreMapperImpl;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = GenresRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalRestExceptionHandler.class
        ))
@AutoConfigureWebTestClient
@Import(GenreMapperImpl.class)
class GenresRestControllerTest {

    @Autowired WebTestClient webTestClient;

    @MockitoBean GenreService genreService;

    @Test
    void list_returnsPage() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());
        var g1 = new Genre("g1", "Fantasy", 0L);
        var g2 = new Genre("g2", "Sci-Fi", 0L);
        var page = new PageImpl<>(List.of(g1, g2), pageable, 10);

        Mockito.when(genreService.findAll(Mockito.any(Pageable.class)))
                .thenReturn(Mono.just(page));

        webTestClient.get().uri("/api/v1/genres?page=0&size=2&sort=name,asc")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[*].id").value(ids ->
                        assertThat(ids)
                                .asInstanceOf(InstanceOfAssertFactories.list(String.class))
                                .containsExactlyInAnyOrder("g1", "g2"))
                .jsonPath("$.content[*].name").value(names ->
                        assertThat(names)
                                .asInstanceOf(InstanceOfAssertFactories.list(String.class))
                                .containsExactlyInAnyOrder("Fantasy", "Sci-Fi"))
                .jsonPath("$.totalElements").isEqualTo(10);
    }

    @Test
    void create_returns201_location_and_body() {
        var form = new GenreForm("New Genre");
        var saved = new Genre("newId", "New Genre", 0L);

        Mockito.when(genreService.insert("New Genre"))
                .thenReturn(Mono.just(saved));

        webTestClient.post().uri("/api/v1/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(form)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/v1/genres/newId")
                .expectBody()
                .jsonPath("$.id").isEqualTo("newId")
                .jsonPath("$.name").isEqualTo("New Genre");
    }
}
