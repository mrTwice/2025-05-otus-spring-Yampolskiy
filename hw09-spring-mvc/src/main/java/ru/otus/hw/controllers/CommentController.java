package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import ru.otus.hw.dto.CommentForm;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books/{bookId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final BookService bookService;
    private final BookMapper bookMapper;
    private final CommentMapper commentMapper;

    @PostMapping
    public String create(@PathVariable long bookId,
                         @Valid @ModelAttribute("commentForm") CommentForm form,
                         BindingResult binding,
                         Model model) {
        if (binding.hasErrors()) {
            var book = bookService.findById(bookId);
            model.addAttribute("book", bookMapper.toDetailsDto(book));
            var comments = commentService.findByBookId(bookId).stream()
                    .map(commentMapper::toDto)
                    .toList();
            model.addAttribute("comments", comments);
            return "book/details";
        }

        commentService.insert(bookId, form.getText());
        return "redirect:/books/%d".formatted(bookId);
    }

    @PostMapping("/{commentId}/delete")
    public String delete(@PathVariable long bookId, @PathVariable long commentId) {
        commentService.deleteById(commentId);
        return "redirect:/books/%d".formatted(bookId);
    }
}

