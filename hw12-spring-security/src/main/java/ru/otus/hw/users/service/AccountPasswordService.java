package ru.otus.hw.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.users.model.User;

@Service
@RequiredArgsConstructor
public class AccountPasswordService {

    private final UserReadService userReadService;

    private final UserAccountService userAccountService;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User changePassword(Long userId, String currentPassword, String newPassword, String confirm) {
        if (!newPassword.equals(confirm)) {
            throw new ValidationException("Passwords do not match");
        }

        var u = userReadService.getById(userId);
        if (currentPassword != null && !currentPassword.isBlank()
                && !passwordEncoder.matches(currentPassword, u.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }
        return userAccountService.changePassword(userId, passwordEncoder.encode(newPassword));
    }
}
