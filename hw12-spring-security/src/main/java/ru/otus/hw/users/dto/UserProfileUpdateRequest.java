package ru.otus.hw.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(min = 3, max = 64) String username,
        @Email String email
) {}