package ru.otus.hw.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

@Component
@RequiredArgsConstructor
public class AuthorRefResolver {
    private final AuthorRepository authorRepository;

    public Author byId(Long id) {
        return (id == null) ? null : authorRepository.findById(id).orElse(null);
    }
}
