package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.AuthorForm;
import ru.otus.hw.mappers.AuthorMapper;
import ru.otus.hw.services.AuthorService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/authors")
public class AuthorsRestController {

    private final AuthorService authorService;

    private final AuthorMapper authorMapper;

    @GetMapping
    public Flux<AuthorDto> list() {
        return authorService.findAll().map(authorMapper::toDto);
    }

    @PostMapping
    public Mono<ResponseEntity<AuthorDto>> create(@Validated @RequestBody AuthorForm form) {
        return authorService.insert(form.getFullName())
                .map(saved -> ResponseEntity
                        .created(URI.create("/api/v1/authors/" + saved.getId()))
                        .body(authorMapper.toDto(saved)));
    }
}
