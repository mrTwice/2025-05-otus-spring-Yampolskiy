package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.BookDetailsDto;
import ru.otus.hw.dto.BookForm;
import ru.otus.hw.dto.CommentForm;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final CommentService commentService;

    private final BookMapper bookMapper;

    private final CommentMapper commentMapper;

    @GetMapping
    public String list(Model model) {
        var items = bookService.findAll().stream()
                .map(bookMapper::toListItemDto)
                .toList();
        model.addAttribute("books", items);
        return "book/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable long id, Model model) {
        Book book = bookService.findById(id);
        BookDetailsDto dto = bookMapper.toDetailsDto(book);
        model.addAttribute("book", dto);

        var comments = commentService.findByBookId(id).stream()
                .map(commentMapper::toDto)
                .toList();
        model.addAttribute("comments", comments);

        model.addAttribute("commentForm", new CommentForm());
        return "book/details";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        prepareRefs(model);
        return "book/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable long id, Model model) {
        Book book = bookService.findById(id);

        var form = BookForm.builder()
                .id(book.getId())
                .title(book.getTitle())
                .authorId(book.getAuthor().getId())
                .genresIds(book.getGenres().stream().map(Genre::getId).collect(Collectors.toCollection(LinkedHashSet::new)))
                .version(book.getVersion())
                .build();

        model.addAttribute("form", form);
        prepareRefs(model);
        return "book/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BookForm form, BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            prepareRefs(model);
            return "book/form";
        }
        var saved = bookService.insert(form.getTitle(), form.getAuthorId(), form.getGenresIds());
        return "redirect:/books/%d".formatted(saved.getId());
    }

    @PostMapping("/{id}")
    public String update(@PathVariable long id,
                         @Valid @ModelAttribute("form") BookForm form, BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            prepareRefs(model);
            return "book/form";
        }
        var saved = bookService.update(id, form.getTitle(), form.getAuthorId(), form.getGenresIds());
        return "redirect:/books/%d".formatted(saved.getId());
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

    private void prepareRefs(Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
    }
}
