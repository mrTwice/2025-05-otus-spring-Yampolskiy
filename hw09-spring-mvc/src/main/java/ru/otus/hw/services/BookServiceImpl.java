package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.AssociationViolationException;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Book findById(long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book with id %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findPageWithAuthorAndGenres(pageable);
    }

    @Override
    @Transactional
    public Book insert(String title, long authorId, Set<Long> genresIds) {
        String normalizedTitle = normalizeAndValidateTitle(title);
        requireNonEmptyGenresIds(genresIds);

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author with id %d not found".formatted(authorId)));

        var genres = findGenresOrThrow(genresIds);

        var book = new Book();
        book.setTitle(normalizedTitle);
        book.setAuthor(author);
        book.replaceGenres(genres);

        return bookRepository.save(book);
    }


    @Override
    @Transactional
    public Book update(long id, String title, long authorId, Set<Long> genresIds) {
        String normalizedTitle = normalizeAndValidateTitle(title);
        requireNonEmptyGenresIds(genresIds);

        var book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book with id %d not found".formatted(id)));

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author with id %d not found".formatted(authorId)));

        var genres = findGenresOrThrow(genresIds);

        book.setTitle(normalizedTitle);
        book.setAuthor(author);
        book.replaceGenres(genres);

        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        try {
            bookRepository.deleteById(id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    "Book with id %d not found".formatted(id), e);
        }
    }


    private Set<Genre> findGenresOrThrow(Set<Long> genresIds) {
        var found = genreRepository.findByIdIn(genresIds);

        if (found.isEmpty()) {
            throw new AssociationViolationException("All genres not found: " + genresIds);
        }

        var foundIds = found.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        if (foundIds.size() != genresIds.size()) {
            var missing = new LinkedHashSet<>(genresIds);
            missing.removeAll(foundIds);
            throw new AssociationViolationException("Some genres not found: " + missing);
        }

        return new LinkedHashSet<>(found);
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

    private void requireNonEmptyGenresIds(Set<Long> genresIds) {
        if (genresIds == null || genresIds.isEmpty()) {
            throw new ValidationException("Book must have at least one genre");
        }
    }

}

