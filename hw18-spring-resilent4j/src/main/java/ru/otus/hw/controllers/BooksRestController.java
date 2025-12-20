package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.otus.hw.dto.BookDetailsDto;
import ru.otus.hw.dto.BookForm;
import ru.otus.hw.dto.BookListItemDto;
import ru.otus.hw.dto.PageResponse;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.services.BookService;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BooksRestController {

    private final BookService bookService;

    private final BookMapper bookMapper;

    @GetMapping
    public PageResponse<BookListItemDto> list(Pageable pageable) {
        return PageResponse.from(
                bookService.findAll(pageable).map(bookMapper::toListItemDto)
        );
    }

    @GetMapping("/{id}")
    public BookDetailsDto details(@PathVariable long id) {
        return bookMapper.toDetailsDto(bookService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookDetailsDto> create(@Validated @RequestBody BookForm form) {
        var saved = bookService.insert(form.getTitle(), form.getAuthorId(), form.getGenresIds());
        var dto = bookMapper.toDetailsDto(saved);
        URI location = URI.create("/api/v1/books/" + saved.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public BookDetailsDto update(@PathVariable long id, @Validated @RequestBody BookForm form) {
        var saved = bookService.update(id, form.getTitle(), form.getAuthorId(), form.getGenresIds(), form.getVersion());
        return bookMapper.toDetailsDto(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        bookService.deleteById(id);
    }
}

