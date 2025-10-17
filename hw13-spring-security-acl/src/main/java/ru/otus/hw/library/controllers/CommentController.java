package ru.otus.hw.library.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import ru.otus.hw.library.dto.CommentForm;
import ru.otus.hw.library.mappers.BookMapper;
import ru.otus.hw.library.mappers.CommentMapper;
import ru.otus.hw.library.services.BookService;
import ru.otus.hw.library.services.CommentService;
import ru.otus.hw.security.model.AppUserDetails;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books/{bookId}/comments")
public class CommentController {

    private final CommentService commentService;

    private final BookService bookService;

    private final BookMapper bookMapper;

    private final CommentMapper commentMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public String create(
            @PathVariable long bookId,
            @Valid @ModelAttribute("commentForm") CommentForm form,
            BindingResult binding,
            Model model,
            @AuthenticationPrincipal AppUserDetails me
    ) {
        if (binding.hasErrors()) {
            var book = bookService.findById(bookId);
            model.addAttribute("book", bookMapper.toDetailsDto(book));
            var comments = commentService.findByBookId(bookId).stream()
                    .map(commentMapper::toDto)
                    .toList();
            model.addAttribute("comments", comments);
            return "book/details";
        }

        commentService.insert(bookId, me.getId(), form.getText());
        return "redirect:/books/%d".formatted(bookId);
    }

    @PostMapping("/{commentId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(
            @PathVariable long bookId,
            @PathVariable long commentId
    ) {
        commentService.deleteById(commentId);
        return "redirect:/books/%d".formatted(bookId);
    }
}

