package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Override
    public Flux<Author> findAll() {
        return authorRepository.findAll(Sort.by("fullName").ascending());
    }

    @Override
    public Mono<Author> insert(String fullName) {
        String normalized = normalize(fullName);
        return authorRepository.save(new Author(null, normalized, 0L));
    }

    @Override
    public Mono<Author> findById(String id) {
        if (id == null) {
            return Mono.error(new ValidationException("Author id must not be null"));
        }
        return authorRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Author with id %s not found".formatted(id))));
    }


    private String normalize(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new ValidationException("Author fullName must not be blank");
        }
        return s.trim();
    }
}
