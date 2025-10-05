package ru.otus.hw.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

@Component
@RequiredArgsConstructor
public class AuthorRefResolver {
    private final AuthorRepository authorRepository;

    public Mono<Author> byId(String id) {
        if (id == null) {
            return Mono.empty();
        }
        return authorRepository.findById(id);
    }
}

