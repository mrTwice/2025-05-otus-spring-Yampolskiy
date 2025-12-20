package ru.otus.java.springframework.yampolskiy.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import ru.otus.java.springframework.yampolskiy.dto.AuthorDto;
import ru.otus.java.springframework.yampolskiy.mappers.AuthorMapper;
import ru.otus.java.springframework.yampolskiy.services.AuthorService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/authors")
public class AuthorsRestController {

    private final AuthorService authorService;

    private final AuthorMapper authorMapper;

    @GetMapping
    public List<AuthorDto> list() {
        return authorService.findAll().stream().map(authorMapper::toDto).toList();
    }
}
