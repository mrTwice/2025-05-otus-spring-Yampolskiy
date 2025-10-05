package ru.otus.hw.users.service;

import ru.otus.hw.users.model.User;

public interface UserAccountService {
    User updateProfile(Long userId, String newUsername, String newEmail);

    User changePassword(Long userId, String newPasswordHash);
}