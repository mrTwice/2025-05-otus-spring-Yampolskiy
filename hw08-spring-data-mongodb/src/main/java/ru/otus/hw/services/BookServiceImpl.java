package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.ConflictException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    @Override
    public Optional<Book> findById(String id) {
        return bookRepository.findById(id);
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book insert(String title, String authorId, Set<String> genresIds) {
        validateTitle(title);
        validateAuthorExists(authorId);
        var genreIdsList = validateAndNormalizeGenreIds(genresIds);

        var book = new Book(null, title.trim(), authorId, genreIdsList);
        try {
            return bookRepository.save(book);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Книга с таким названием для этого автора уже существует");
        }
    }

    @Override
    public Book update(String id, String title, String authorId, Set<String> genresIds) {
        var existing = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(id)));

        validateTitle(title);
        validateAuthorExists(authorId);
        var genreIdsList = validateAndNormalizeGenreIds(genresIds);

        existing.setTitle(title.trim());
        existing.setAuthorId(authorId);
        existing.setGenreIds(genreIdsList);

        try {
            return bookRepository.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Книга с таким названием для этого автора уже существует");
        }
    }

    @Override
    public void deleteById(String id) {
        if (!bookRepository.existsById(id)) {
            return;
        }
        commentRepository.deleteByBookId(id);
        bookRepository.deleteById(id);
    }


    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Title must not be null or blank");
        }
    }

    private void validateAuthorExists(String authorId) {
        if (authorId == null || authorId.isBlank()) {
            throw new ValidationException("Author id must not be null or blank");
        }
        if (!authorRepository.existsById(authorId)) {
            throw new EntityNotFoundException("Author with id %s not found".formatted(authorId));
        }
    }

    private List<String> validateAndNormalizeGenreIds(Set<String> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            throw new ValidationException("Genres ids must not be null or empty");
        }
        var found = genreRepository.findByIdIn(genreIds);
        if (found.size() != genreIds.size()) {
            var foundIds = found.stream().map(Genre::getId).collect(Collectors.toSet());
            var missing = new java.util.HashSet<>(genreIds);
            missing.removeAll(foundIds);
            throw new EntityNotFoundException("Genres not found by ids: %s".formatted(missing));
        }
        return genreIds.stream().distinct().sorted().toList();
    }
}




