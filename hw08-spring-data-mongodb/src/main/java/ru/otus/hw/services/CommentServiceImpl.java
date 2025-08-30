package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    public Optional<Comment> findById(String id) {
        return commentRepository.findById(id);
    }

    @Override
    public List<Comment> findByBookId(String bookId) {
        validateBookId(bookId);
        if (!bookRepository.existsById(bookId)) {
            throw new EntityNotFoundException("Book with id %s not found".formatted(bookId));
        }
        return commentRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    @Override
    public Comment insert(String bookId, String text) {
        validateText(text);
        validateBookId(bookId);
        if (!bookRepository.existsById(bookId)) {
            throw new EntityNotFoundException("Book with id %s not found".formatted(bookId));
        }

        var comment = new Comment(
                null,
                text.trim(),
                Instant.now(),
                bookId
        );
        return commentRepository.save(comment);
    }

    @Override
    public Comment update(String id, String text) {
        validateText(text);

        var existing = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));

        existing.setText(text.trim());
        return commentRepository.save(existing);
    }

    @Override
    public void deleteById(String id) {
        if (!commentRepository.existsById(id)) {
            return;
        }
        commentRepository.deleteById(id);
    }

    private void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Comment text must not be null or blank");
        }
    }

    private void validateBookId(String bookId) {
        if (bookId == null || bookId.isBlank()) {
            throw new ValidationException("Book id must not be null or blank");
        }
    }
}


