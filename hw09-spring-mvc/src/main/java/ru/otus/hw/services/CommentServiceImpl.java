package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Comment findById(long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment with id %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByBookId(long bookId) {
        return commentRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> findByBookId(long bookId, Pageable page) {
        return commentRepository.findByBookId(bookId, page);
    }

    @Override
    @Transactional
    public Comment insert(long bookId, String text) {
        String normalized = normalizeAndValidateText(text);

        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException(
                        "Book with id %d not found".formatted(bookId)));

        var comment = new Comment(normalized, book);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment update(long id, String text) {
        String normalized = normalizeAndValidateText(text);

        var existing = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Comment with id %d not found".formatted(id)));

        existing.setText(normalized);
        return commentRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        try {
            commentRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    "Comment with id %d not found".formatted(id), e);
        }
    }


    private String normalizeAndValidateText(String text) {
        if (text == null) {
            throw new ValidationException("Comment text must not be blank");
        }
        var trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException("Comment text must not be blank");
        }
        if (trimmed.length() > 2048) {
            throw new ValidationException("Comment text must be ≤ 2048 characters");
        }
        return trimmed;
    }
}

