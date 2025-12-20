package ru.otus.java.springframework.yampolskiy.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.java.springframework.yampolskiy.models.Author;
import ru.otus.java.springframework.yampolskiy.repositories.AuthorRepository;

@Component
@RequiredArgsConstructor
public class AuthorRefResolver {
    private final AuthorRepository authorRepository;

    public Author byId(Long id) {
        return (id == null) ? null : authorRepository.findById(id).orElse(null);
    }
}
