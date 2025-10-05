package ru.otus.hw.users.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.otus.hw.security.model.AppUserDetails;
import ru.otus.hw.security.service.SessionKiller;
import ru.otus.hw.users.dto.ChangePasswordRequest;
import ru.otus.hw.users.dto.UserProfileUpdateRequest;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.service.AccountPasswordService;
import ru.otus.hw.users.service.UserAccountService;
import ru.otus.hw.users.service.UserReadService;

@PreAuthorize("isAuthenticated()")
@Controller
@AllArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final UserReadService userReadService;

    private final UserAccountService userAccountService;

    private final AccountPasswordService accountPasswordService;

    private final SessionKiller sessionKiller;

    @GetMapping
    public String view(
            @AuthenticationPrincipal AppUserDetails me,
            Model model
    ) {
        User user = userReadService.getById(me.getId());
        model.addAttribute("user", user);
        return "user/profile";
    }

    @GetMapping("/edit")
    public String editForm(
            @AuthenticationPrincipal AppUserDetails me,
            Model model
    ) {
        User user = userReadService.getById(me.getId());
        var form = new UserProfileUpdateRequest(user.getUsername(), user.getEmail());
        model.addAttribute("form", form);
        return "user/profile-edit";
    }

    @PostMapping
    public String update(
            @AuthenticationPrincipal AppUserDetails me,
            @Valid @ModelAttribute("form") UserProfileUpdateRequest form,
            BindingResult binding
    ) {
        if (binding.hasErrors()) {
            return "user/profile-edit";
        }
        userAccountService.updateProfile(me.getId(), form.username(), form.email());
        return "redirect:/profile?updated";
    }

    @PostMapping("/password")
    public String changePassword(
            @AuthenticationPrincipal AppUserDetails me,
            @Valid @ModelAttribute("req") ChangePasswordRequest req,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            BindingResult binding
    ) {

        if (binding.hasErrors()) {
            return "user/profile";
        }

        accountPasswordService.changePassword(
                me.getId(),
                req.currentPassword(),
                req.newPassword(),
                req.confirmPassword());
        sessionKiller.expireOnPasswordChange(me.getUsername(), request, response, authentication);
        return "redirect:/login?pwdChanged";
    }
}

