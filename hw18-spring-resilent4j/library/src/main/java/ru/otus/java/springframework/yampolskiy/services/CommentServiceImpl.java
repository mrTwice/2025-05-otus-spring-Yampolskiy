package ru.otus.java.springframework.yampolskiy.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.java.springframework.yampolskiy.exceptions.CommentForbiddenException;
import ru.otus.java.springframework.yampolskiy.exceptions.NotFoundException;
import ru.otus.java.springframework.yampolskiy.exceptions.ValidationException;
import ru.otus.java.springframework.yampolskiy.integretion.ModerationClient;
import ru.otus.java.springframework.yampolskiy.integretion.dto.ModerationResponse;
import ru.otus.java.springframework.yampolskiy.models.Comment;
import ru.otus.java.springframework.yampolskiy.repositories.BookRepository;
import ru.otus.java.springframework.yampolskiy.repositories.CommentRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final ModerationClient moderationClient;

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
                .orElseThrow(() -> new NotFoundException("Book with id %d not found".formatted(bookId)));

        ModerationResponse mr = moderationClient.check(normalized);

        if (!mr.allowed()) {
            throw new CommentForbiddenException("Comment is not allowed: " + mr.reason());
        }

        var comment = new Comment(normalized, book);
        return commentRepository.save(comment);
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

