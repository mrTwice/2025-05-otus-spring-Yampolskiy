package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.AssociationViolationException;
import ru.otus.hw.exceptions.ConflictException;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private  final CommentRepository commentRepository;

    @Override
    public Mono<Book> findById(String id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Book with id %s not found".formatted(id))));
    }

    @Override
    public Flux<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Mono<Page<Book>> findAll(Pageable pageable) {
        Mono<Long> total = bookRepository.count();
        Mono<List<Book>> content = bookRepository.findAll()
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .take(pageable.getPageSize())
                .collectList();

        return Mono.zip(content, total)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Book> insert(String title, String authorId, Set<String> genresIds) {
        String normalizedTitle = normalizeAndValidateTitle(title);
        requireNonEmptyGenresIds(genresIds);

        Mono<Boolean> authorOk = authorRepository.existsById(authorId)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new NotFoundException("Author with id %s not found".formatted(authorId))));

        Mono<List<String>> genresOk = ensureGenresExist(genresIds);

        return Mono.zip(authorOk, genresOk)
                .flatMap(t -> {
                    Book book = new Book();
                    book.setTitle(normalizedTitle);
                    book.setAuthorId(authorId);
                    book.setGenresIds(new ArrayList<>(t.getT2()));
                    book.setVersion(0L);
                    return bookRepository.save(book);
                });
    }

    @Override
    public Mono<Book> update(String id, String title, String authorId, Set<String> genresIds, long expectedVersion) {
        String normalizedTitle = normalizeAndValidateTitle(title);
        requireNonEmptyGenresIds(genresIds);
        Mono<Book> existingMono = bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Book with id %s not found".formatted(id))));
        Mono<Boolean> authorOk = authorRepository.existsById(authorId)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new NotFoundException("Author with id %s not found".formatted(authorId))));
        Mono<List<String>> genresOk = ensureGenresExist(genresIds);

        return Mono.zip(existingMono, authorOk, genresOk)
                .flatMap(tuple -> {
                    Book existing = tuple.getT1();
                    if (expectedVersion != existing.getVersion()) {
                        return Mono.error(new ConflictException(
                                "Stale version: expected %d but was %d"
                                        .formatted(expectedVersion, existing.getVersion())));
                    }
                    existing.setTitle(normalizedTitle);
                    existing.setAuthorId(authorId);
                    existing.setGenresIds(new ArrayList<>(tuple.getT3()));
                    return bookRepository.save(existing);
                });
    }

    private Mono<List<String>> ensureGenresExist(Set<String> genresIds) {
        return genreRepository.findByIdIn(genresIds)
                .map(Genre::getId)
                .collectList()
                .flatMap(foundIds -> {
                    if (foundIds.isEmpty()) {
                        return Mono.error(new AssociationViolationException("All genres not found: " + genresIds));
                    }

                    Set<String> foundSet = new LinkedHashSet<>(foundIds);
                    if (foundSet.size() != genresIds.size()) {
                        Set<String> missing = new LinkedHashSet<>(genresIds);
                        missing.removeAll(foundSet);
                        return Mono.error(new AssociationViolationException("Some genres not found: " + missing));
                    }
                    return Mono.just(foundIds);
                });
    }


    @Override
    public Mono<Void> deleteById(String id) {
        return bookRepository.existsById(id)
                .flatMap(exists -> exists
                        ? commentRepository.deleteByBookId(id).then(bookRepository.deleteById(id))
                        : Mono.<Void>error(new NotFoundException("Book with id %s not found".formatted(id))))
                .then();
    }


    private Mono<List<Genre>> findGenresOrThrow(Set<String> genresIds) {
        return genreRepository.findByIdIn(genresIds)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.error(new AssociationViolationException("All genres not found: " + genresIds));
                    }
                    var foundIds = list.stream().map(Genre::getId).collect(Collectors.toSet());
                    if (foundIds.size() != genresIds.size()) {
                        var missing = new LinkedHashSet<>(genresIds);
                        missing.removeAll(foundIds);
                        return Mono.error(new AssociationViolationException("Some genres not found: " + missing));
                    }
                    return Mono.just(list);
                });
    }

    private String normalizeAndValidateTitle(String title) {
        if (title == null) {
            throw new ValidationException("Title must not be blank");
        }
        var trimmed = title.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException("Title must not be blank");
        }
        return trimmed;
    }

    private void requireNonEmptyGenresIds(Set<String> genresIds) {
        if (genresIds == null || genresIds.isEmpty()) {
            throw new ValidationException("Book must have at least one genre");
        }
    }
}
