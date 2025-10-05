package ru.otus.hw.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        @NotBlank(message = "Current password must not be blank")
        String currentPassword,

        @NotBlank(message = "New password must not be blank")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        String newPassword,

        @NotBlank(message = "Confirm password must not be blank")
        String confirmPassword
) {}