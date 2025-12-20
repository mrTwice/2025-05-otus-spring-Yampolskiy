package ru.otus.hw.users.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.users.model.User;

@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final PasswordEncoder passwordEncoder;

    private final UserAdminService userAdminService;

    @Override
    @Transactional
    public User register(User userWithRawPassword) {
        if (userWithRawPassword == null) {
            throw new ValidationException("User must not be null");
        }
        String raw = userWithRawPassword.getPasswordHash();
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Password must not be blank");
        }

        userWithRawPassword.setPasswordHash(passwordEncoder.encode(raw));

        return userAdminService.createUser(userWithRawPassword);
    }
}
