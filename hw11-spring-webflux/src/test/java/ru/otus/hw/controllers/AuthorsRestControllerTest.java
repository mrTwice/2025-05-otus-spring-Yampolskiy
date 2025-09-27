package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.components.GlobalRestExceptionHandler;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.AuthorForm;
import ru.otus.hw.mappers.AuthorMapperImpl;
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;

import static org.assertj.core.api.Assertions.assertThat;


@WebFluxTest(controllers = AuthorsRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalRestExceptionHandler.class
        ))
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import(AuthorMapperImpl.class)
class AuthorsRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthorService authorService;

    @Test
    void list_returnsDtos() {
        var a1 = new Author("id1", "Author_1", 0L);
        var a2 = new Author("id2", "Author_2", 0L);

        Mockito.when(authorService.findAll())
                .thenReturn(Flux.just(a1, a2));

        webTestClient.get().uri("/api/v1/authors")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuthorDto.class)
                .value(list -> {
                    assertThat(list).hasSize(2);
                    assertThat(list).extracting(AuthorDto::getId).containsExactlyInAnyOrder("id1", "id2");
                    assertThat(list).extracting(AuthorDto::getFullName).containsExactlyInAnyOrder("Author_1", "Author_2");
                });
    }

    @Test
    void create_returns201_andLocation_andBody() {
        var form = new AuthorForm("New Author");
        var saved = new Author("newId", "New Author", 0L);

        Mockito.when(authorService.insert("New Author"))
                .thenReturn(Mono.just(saved));

        webTestClient.post().uri("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(form)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/v1/authors/newId")
                .expectBody(AuthorDto.class)
                .value(dto -> {
                    assertThat(dto.getId()).isEqualTo("newId");
                    assertThat(dto.getFullName()).isEqualTo("New Author");
                });
    }
}
