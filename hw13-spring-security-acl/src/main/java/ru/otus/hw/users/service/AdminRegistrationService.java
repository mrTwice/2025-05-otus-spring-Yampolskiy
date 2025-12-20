package ru.otus.hw.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.users.model.User;

@Service
@RequiredArgsConstructor
public class AdminRegistrationService {

    private final UserAdminService userAdminService;

    private final PasswordEncoder passwordEncoder;


    @Transactional
    public User create(User user) {
        if (user == null) {
            throw new ValidationException("Request must not be null");
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userAdminService.createUser(user);
    }
}
