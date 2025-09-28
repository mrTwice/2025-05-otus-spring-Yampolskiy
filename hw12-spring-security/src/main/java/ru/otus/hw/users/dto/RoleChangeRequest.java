package ru.otus.hw.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoleChangeRequest(
        @NotBlank @Pattern(regexp = "ADMIN|READER") String role
) {}