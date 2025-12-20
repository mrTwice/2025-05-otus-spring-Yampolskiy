package ru.otus.hw.users.service;

import ru.otus.hw.users.model.User;

public interface UserRegistrationService {
    User register(User userWithRawPassword);
}