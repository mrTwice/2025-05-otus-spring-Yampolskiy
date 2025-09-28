package ru.otus.hw.users.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.hw.users.model.User;

public interface UserReadService {
    User getById(Long id);

    User getByUsername(String username);

    User getByEmail(String email);

    User getByUsernameOrEmail(String value);

    Page<User> getPage(Pageable pageable);
}