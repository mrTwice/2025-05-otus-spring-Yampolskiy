package ru.otus.hw.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.security.model.AppUserDetails;
import ru.otus.hw.users.dto.UserProfileUpdateRequest;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.service.AccountPasswordService;
import ru.otus.hw.users.service.UserAccountService;
import ru.otus.hw.users.service.UserReadService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final UserReadService userReadService;

    private final UserAccountService userAccountService;

    private final AccountPasswordService accountPasswordService;

    @GetMapping
    public String view(@AuthenticationPrincipal AppUserDetails me, Model model) {
        User user = userReadService.getById(me.getId());
        model.addAttribute("user", user);
        return "user/profile";
    }

    @GetMapping("/edit")
    public String editForm(@AuthenticationPrincipal AppUserDetails me, Model model) {
        User user = userReadService.getById(me.getId());
        var form = new UserProfileUpdateRequest(user.getUsername(), user.getEmail());
        model.addAttribute("form", form);
        return "user/profile-edit";
    }

    @PostMapping
    public String update(@AuthenticationPrincipal AppUserDetails me,
                         @Valid @ModelAttribute("form") UserProfileUpdateRequest form,
                         BindingResult binding) {
        if (binding.hasErrors()) {
            return "user/profile-edit";
        }
        userAccountService.updateProfile(me.getId(), form.username(), form.email());
        return "redirect:/profile?updated";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal AppUserDetails me,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword) {
        accountPasswordService.changePassword(me.getId(),
                newPassword, confirmPassword, currentPassword);
        return "redirect:/profile?pwdChanged";
    }
}

