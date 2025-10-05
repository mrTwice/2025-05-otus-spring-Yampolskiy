package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.GenreForm;
import ru.otus.hw.dto.PageResponse;
import ru.otus.hw.mappers.GenreMapper;
import ru.otus.hw.services.GenreService;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenresRestController {

    private final GenreService genreService;

    private final GenreMapper genreMapper;

    @GetMapping
    public Mono<PageResponse<GenreDto>> list(Pageable pageable) {
        return genreService.findAll(pageable)
                .map(page -> PageResponse.from(
                        page.map(genreMapper::toDto)
                ));
    }

    @PostMapping
    public Mono<ResponseEntity<GenreDto>> create(@Validated @RequestBody GenreForm form) {
        return genreService.insert(form.getName())
                .map(saved -> ResponseEntity
                        .created(URI.create("/api/v1/genres/" + saved.getId()))
                        .body(genreMapper.toDto(saved)));
    }
}
