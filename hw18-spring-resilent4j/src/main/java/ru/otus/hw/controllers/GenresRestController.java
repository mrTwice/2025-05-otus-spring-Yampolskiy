package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.PageResponse;
import ru.otus.hw.mappers.GenreMapper;
import ru.otus.hw.services.GenreService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/genres")
public class GenresRestController {

    private final GenreService genreService;

    private final GenreMapper genreMapper;

    @GetMapping
    public PageResponse<GenreDto> list(Pageable pageable) {
        return PageResponse.from(
                genreService.findAll(pageable).map(genreMapper::toDto)
        );
    }
}
