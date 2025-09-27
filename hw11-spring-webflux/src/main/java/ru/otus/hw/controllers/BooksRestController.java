package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDetailsDto;
import ru.otus.hw.dto.BookForm;
import ru.otus.hw.dto.BookListItemDto;
import ru.otus.hw.dto.PageResponse;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BooksRestController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final BookMapper bookMapper;

    @GetMapping
    public Mono<PageResponse<BookListItemDto>> list(Pageable pageable) {
        return bookService.findAll(pageable)
                .flatMap((Page<Book> page) ->
                        Flux.fromIterable(page.getContent())
                                .flatMap(book ->
                                        Mono.zip(
                                                authorService.findById(book.getAuthorId())
                                                        .map(Author::getFullName),
                                                genreService.findByIds(new LinkedHashSet<>(book.getGenresIds()))
                                        ).map(t -> {
                                            String authorFullName = t.getT1();
                                            List<Genre> genres = t.getT2();
                                            return bookMapper.toListItemDto(book, authorFullName, genres);
                                        })
                                )
                                .collectList()
                                .map(list -> PageResponse.from(
                                        new PageImpl<>(list, pageable, page.getTotalElements())
                                ))
                );
    }

    @GetMapping("/{id}")
    public Mono<BookDetailsDto> details(@PathVariable String id) {
        return bookService.findById(id)
                .flatMap(book ->
                        Mono.zip(
                                authorService.findById(book.getAuthorId()),
                                genreService.findByIds(new LinkedHashSet<>(book.getGenresIds()))
                        ).map(t -> {
                            Author author = t.getT1();
                            List<Genre> genres = t.getT2();
                            return bookMapper.toDetailsDto(book, author, genres);
                        })
                );
    }

    @PostMapping
    public Mono<ResponseEntity<BookDetailsDto>> create(@Validated @RequestBody BookForm form) {
        return bookService.insert(form.getTitle(), form.getAuthorId(), form.getGenresIds())
                .flatMap(saved ->
                        Mono.zip(
                                authorService.findById(saved.getAuthorId()),
                                genreService.findByIds(new LinkedHashSet<>(saved.getGenresIds()))
                        ).map(t -> {
                            BookDetailsDto dto = bookMapper.toDetailsDto(saved, t.getT1(), t.getT2());
                            URI location = URI.create("/api/v1/books/" + saved.getId());
                            return ResponseEntity.created(location).body(dto);
                        })
                );
    }

    @PutMapping("/{id}")
    public Mono<BookDetailsDto> update(@PathVariable String id, @Validated @RequestBody BookForm form) {
        return bookService.update(id, form.getTitle(), form.getAuthorId(), form.getGenresIds(), form.getVersion())
                .flatMap(saved ->
                        Mono.zip(
                                authorService.findById(saved.getAuthorId()),
                                genreService.findByIds(new LinkedHashSet<>(saved.getGenresIds()))
                        ).map(t -> bookMapper.toDetailsDto(saved, t.getT1(), t.getT2()))
                );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return bookService.deleteById(id);
    }
}
