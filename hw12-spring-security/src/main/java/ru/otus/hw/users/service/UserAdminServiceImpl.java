package ru.otus.hw.users.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.otus.hw.exceptions.AssociationViolationException;
import ru.otus.hw.exceptions.ConflictException;
import ru.otus.hw.exceptions.DuplicateException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository users;
    private final UserReadService reader;

    @Override
    public User createUser(User userWithHashedPassword) {
        if (userWithHashedPassword == null) {
            throw new ValidationException("User is null");
        }
        requireText(userWithHashedPassword.getUsername(), "username");
        requireText(userWithHashedPassword.getEmail(), "email");
        requireText(userWithHashedPassword.getPasswordHash(), "passwordHash");

        String username = userWithHashedPassword.getUsername();
        String email = userWithHashedPassword.getEmail().toLowerCase();

        if (users.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateException("Username already taken: " + username);
        }
        if (users.existsByEmailIgnoreCase(email)) {
            throw new DuplicateException("Email already in use: " + email);
        }

        userWithHashedPassword.setEmail(email);

        Set<String> roles = normalizeRoles(userWithHashedPassword.getRoles());
        if (roles.isEmpty()) roles = Set.of("READER");
        userWithHashedPassword.setRoles(roles);

        try {
            return users.save(userWithHashedPassword);
        } catch (Exception e) {
            throw new ConflictException("Failed to create user", e);
        }
    }

    @Override
    public User setEnabled(Long userId, boolean enabled) {
        User u = reader.getById(userId);
        u.setEnabled(enabled);
        try {
            return users.save(u);
        } catch (Exception e) {
            throw new ConflictException("Failed to set enabled=" + enabled + " for id=" + userId, e);
        }
    }

    @Override
    public User addRole(Long userId, String role) {
        User u = reader.getById(userId);
        String norm = normalizeRole(role);
        Set<String> rs = new HashSet<>(u.getRoles());
        if (rs.add(norm)) {
            u.setRoles(rs);
            try {
                return users.save(u);
            } catch (Exception e) {
                throw new ConflictException("Failed to add role '" + norm + "' to id=" + userId, e);
            }
        }
        return u;
    }

    @Override
    public User removeRole(Long userId, String role) {
        User u = reader.getById(userId);
        String norm = normalizeRole(role);
        Set<String> rs = new HashSet<>(u.getRoles());
        if (!rs.remove(norm)) {
            throw new ValidationException("Role not assigned: " + norm);
        }
        u.setRoles(rs);
        try {
            return users.save(u);
        } catch (Exception e) {
            throw new ConflictException("Failed to remove role '" + norm + "' from id=" + userId, e);
        }
    }

    @Override
    public void delete(Long userId) {
        try {
            users.deleteById(userId);
        } catch (org.springframework.dao.DataIntegrityViolationException dive) {
            throw new AssociationViolationException("User has related data, deletion is not allowed", dive);
        } catch (Exception e) {
            throw new ConflictException("Failed to delete user id=" + userId, e);
        }
    }

    private static void requireText(String v, String field) {
        if (!StringUtils.hasText(v)) {
            throw new ValidationException("Field '" + field + "' must not be blank");
        }
    }

    private static Set<String> normalizeRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) return Set.of();
        Set<String> r = new HashSet<>();
        for (String s : roles) {
            if (StringUtils.hasText(s)) {
                r.add(normalizeRole(s));
            }
        }
        return Set.copyOf(r);
    }

    private static String normalizeRole(String role) {
        String r = role == null ? "" : role.trim();
        if (r.isEmpty()) {
            throw new ValidationException("Role must not be blank");
        }
        if (r.startsWith("ROLE_")) {
            r = r.substring("ROLE_".length());
        }
        return r.toUpperCase();
    }
}

