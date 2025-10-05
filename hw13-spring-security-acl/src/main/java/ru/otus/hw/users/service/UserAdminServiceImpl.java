package ru.otus.hw.users.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        validateNotNull(userWithHashedPassword);
        normalizeAndValidateFields(userWithHashedPassword);
        ensureUniqueness(userWithHashedPassword.getUsername(), userWithHashedPassword.getEmail());
        applyRolesOrDefault(userWithHashedPassword);
        return saveOrConflict(userWithHashedPassword, "Failed to create user");
    }

    @Override
    public User setEnabled(Long userId, boolean enabled) {
        User u = reader.getById(userId);
        u.setEnabled(enabled);
        return saveOrConflict(u, "Failed to set enabled=" + enabled + " for id=" + userId);
    }

    @Override
    public User addRole(Long userId, String role) {
        User u = reader.getById(userId);
        String norm = normalizeRole(role);
        Set<String> rs = new HashSet<>(u.getRoles());
        if (rs.add(norm)) {
            u.setRoles(rs);
            return saveOrConflict(u, "Failed to add role '" + norm + "' to id=" + userId);
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
        return saveOrConflict(u, "Failed to remove role '" + norm + "' from id=" + userId);
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


    private static void validateNotNull(User u) {
        if (u == null) {
            throw new ValidationException("User is null");
        }
    }

    private static void normalizeAndValidateFields(User u) {
        requireText(u.getUsername(), "username");
        requireText(u.getEmail(), "email");
        requireText(u.getPasswordHash(), "passwordHash");
        u.setEmail(u.getEmail().toLowerCase());
    }

    private void ensureUniqueness(String username, String emailLower) {
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateException("Username already taken: " + username);
        }
        if (users.existsByEmailIgnoreCase(emailLower)) {
            throw new DuplicateException("Email already in use: " + emailLower);
        }
    }

    private static void applyRolesOrDefault(User u) {
        Set<String> roles = normalizeRoles(u.getRoles());
        if (roles.isEmpty()) {
            roles = Set.of("READER");
        }
        u.setRoles(roles);
    }

    private User saveOrConflict(User u, String message) {
        try {
            return users.save(u);
        } catch (Exception e) {
            throw new ConflictException(message, e);
        }
    }

    private static void requireText(String v, String field) {
        if (!org.springframework.util.StringUtils.hasText(v)) {
            throw new ValidationException("Field '" + field + "' must not be blank");
        }
    }

    private static Set<String> normalizeRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        Set<String> r = new HashSet<>();
        for (String s : roles) {
            if (org.springframework.util.StringUtils.hasText(s)) {
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

