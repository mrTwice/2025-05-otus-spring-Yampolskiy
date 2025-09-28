package ru.otus.hw.users.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.repository.UserRepository;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReadServiceImpl implements UserReadService {

    private final UserRepository users;

    @Override public User getById(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + id));
    }

    @Override public User getByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    @Override public User getByEmail(String email) {
        return users.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    @Override public User getByUsernameOrEmail(String value) {
        String lowered = value.toLowerCase();
        return users.findByUsernameOrEmail(value, lowered)
                .orElseThrow(() -> new NotFoundException("User not found: " + value));
    }

    @Override public Page<User> getPage(Pageable pageable) {
        return users.findAll(pageable);
    }
}
