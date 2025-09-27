package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    public Mono<Comment> findById(String id) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Comment with id %s not found".formatted(id))));
    }

    @Override
    public Flux<Comment> findByBookId(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return Flux.error(new ValidationException("Book id must not be null or blank"));
        }
        return bookRepository.existsById(bookId)
                .flatMapMany(exists -> exists
                        ? commentRepository.findByBookIdOrderByCreatedAtDesc(bookId)
                        : Flux.error(new NotFoundException("Book with id %s not found".formatted(bookId))));
    }

    @Override
    public Mono<Page<Comment>> findByBookId(String bookId, Pageable page) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return Mono.error(new ValidationException("Book id must not be null or blank"));
        }
        return bookRepository.existsById(bookId)
                .flatMap(exists -> exists
                                ? Mono.zip(
                                commentRepository.findByBookIdOrderByCreatedAtDesc(bookId)
                                        .skip(page.getOffset())
                                        .take(page.getPageSize())
                                        .collectList(),
                                commentRepository.countByBookId(bookId)
                        ).map(t -> new PageImpl<>(t.getT1(), page, t.getT2()))
                                : Mono.error(new NotFoundException("Book with id %s not found".formatted(bookId)))
                );
    }


    @Override
    public Mono<Comment> insert(String bookId, String text) {
        return Mono.defer(() -> {
            if (bookId == null || bookId.trim().isEmpty()) {
                return Mono.error(new ValidationException("Book id must not be null or blank"));
            }
            String normalized = normalizeAndValidateText(text);

            return bookRepository.existsById(bookId)
                    .flatMap(exists -> exists
                            ? commentRepository.save(new Comment(null, normalized, null, bookId, 0L))
                            : Mono.error(new NotFoundException("Book with id %s not found".formatted(bookId))));
        });
    }



    @Override
    public Mono<Comment> update(String id, String text) {
        return Mono.defer(() -> {
            String normalized = normalizeAndValidateText(text);

            return commentRepository.findById(id)
                    .switchIfEmpty(Mono.error(new NotFoundException("Comment with id %s not found".formatted(id))))
                    .flatMap(existing -> {
                        existing.setText(normalized);
                        return commentRepository.save(existing);
                    });
        });
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return commentRepository.existsById(id)
                .flatMap(exists -> exists
                        ? commentRepository.deleteById(id)
                        : Mono.empty()
                );
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

