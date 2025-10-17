package ru.otus.hw.library.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.library.models.Author;
import ru.otus.hw.library.repositories.AuthorRepository;

@Component
@RequiredArgsConstructor
public class AuthorRefResolver {
    private final AuthorRepository authorRepository;

    public Author byId(Long id) {
        return (id == null) ? null : authorRepository.findById(id).orElse(null);
    }
}
