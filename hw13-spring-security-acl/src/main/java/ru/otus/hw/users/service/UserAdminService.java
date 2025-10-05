package ru.otus.hw.users.service;

import ru.otus.hw.users.model.User;

public interface UserAdminService {
    User createUser(User userWithHashedPassword);

    User setEnabled(Long userId, boolean enabled);

    User addRole(Long userId, String role);

    User removeRole(Long userId, String role);

    void delete(Long userId);
}