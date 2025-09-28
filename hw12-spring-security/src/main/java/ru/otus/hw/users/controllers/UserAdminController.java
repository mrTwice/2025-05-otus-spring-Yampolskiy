package ru.otus.hw.users.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.service.UserAdminService;
import ru.otus.hw.users.service.UserReadService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserReadService userReadService;
    private final UserAdminService userAdminService;

    @GetMapping("/users")
    public String list(Model model) {
        List<User> users = userReadService.getPage(PageRequest.of(0, 1000)).getContent();
        model.addAttribute("users", users);
        return "user/list";
    }

    @PostMapping("/users/{id}/enable")
    public String enable(@PathVariable Long id, @RequestParam boolean enabled) {
        userAdminService.setEnabled(id, enabled);
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/roles/add")
    public String addRole(@PathVariable Long id, @RequestParam String role) {
        userAdminService.addRole(id, role);
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/roles/remove")
    public String removeRole(@PathVariable Long id, @RequestParam String role) {
        userAdminService.removeRole(id, role);
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/delete")
    public String delete(@PathVariable Long id) {
        userAdminService.delete(id);
        return "redirect:/users";
    }
}

