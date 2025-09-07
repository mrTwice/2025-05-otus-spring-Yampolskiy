package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import ru.otus.hw.dto.CommentForm;
import ru.otus.hw.services.CommentService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books/{bookId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public String create(@PathVariable long bookId,
                         @Valid @ModelAttribute("commentForm") CommentForm form,
                         BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            return "redirect:/books/%d".formatted(bookId);
        }
        commentService.insert(bookId, form.getText());
        return "redirect:/books/%d".formatted(bookId);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long bookId, @PathVariable long id) {
        commentService.deleteById(id);
        return "redirect:/books/%d".formatted(bookId);
    }
}
