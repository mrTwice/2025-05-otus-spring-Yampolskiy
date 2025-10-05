package ru.otus.hw.users.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.hw.security.model.AppUserDetails;
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
    public String enable(
            @AuthenticationPrincipal AppUserDetails me,
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        if (me != null && id.equals(me.getId()) && !enabled) {
            return "redirect:/users?error=selfDisableForbidden";
        }
        userAdminService.setEnabled(id, enabled);
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/roles/add")
    public String addRole(
            @PathVariable Long id,
            @RequestParam String role
    ) {
        userAdminService.addRole(id, role);
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/roles/remove")
    public String removeRole(
            @AuthenticationPrincipal AppUserDetails me,
            @PathVariable Long id,
            @RequestParam String role
    ) {
        if (me != null && id.equals(me.getId()) && "ADMIN".equalsIgnoreCase(role)) {
            return "redirect:/users?error=selfAdminRemovalForbidden";
        }
        userAdminService.removeRole(id, role);
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/delete")
    public String delete(
            @AuthenticationPrincipal AppUserDetails me,
            @PathVariable Long id
    ) {
        if (me != null && id.equals(me.getId())) {
            return "redirect:/users?error=selfDeleteForbidden";
        }
        userAdminService.delete(id);
        return "redirect:/users";
    }
}

