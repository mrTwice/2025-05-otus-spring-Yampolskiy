package ru.otus.hw.users.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.otus.hw.exceptions.ConflictException;
import ru.otus.hw.exceptions.DuplicateException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountServiceImpl implements UserAccountService {

    private final UserRepository users;

    private final UserReadService reader;

    @Override
    public User updateProfile(Long userId, String newUsername, String newEmail) {
        User u = reader.getById(userId);
        if (!StringUtils.hasText(newUsername) && !StringUtils.hasText(newEmail)) {
            throw new ValidationException("Nothing to update");
        }
        if (StringUtils.hasText(newUsername) &&
                users.existsByUsernameIgnoreCaseAndIdNot(newUsername, userId)) {
            throw new DuplicateException("Username already taken: " + newUsername);
        }
        if (StringUtils.hasText(newEmail)) {
            String lowered = newEmail.toLowerCase();
            if (users.existsByEmailIgnoreCaseAndIdNot(lowered, userId)) {
                throw new DuplicateException("Email already in use: " + newEmail);
            }
            u.setEmail(lowered);
        }
        if (StringUtils.hasText(newUsername)) {
            u.setUsername(newUsername);
        }
        try {
            return users.save(u);
        } catch (Exception e) {
            throw new ConflictException("Failed to update profile: id=" + userId, e);
        }
    }

    @Override
    public User changePassword(Long userId, String newPasswordHash) {
        if (!StringUtils.hasText(newPasswordHash)) {
            throw new ValidationException("newPasswordHash must not be blank");
        }
        User u = reader.getById(userId);
        u.setPasswordHash(newPasswordHash);
        try {
            return users.save(u);
        } catch (Exception e) {
            throw new ConflictException("Failed to change password: id=" + userId, e);
        }
    }
}
