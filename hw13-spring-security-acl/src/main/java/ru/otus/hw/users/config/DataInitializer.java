package ru.otus.hw.users.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.repository.UserRepository;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    public CommandLineRunner initUsers() {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@example.com")
                        .passwordHash(passwordEncoder.encode("admin123"))
                        .enabled(true)
                        .roles(Set.of("ADMIN", "READER"))
                        .build();
                userRepository.save(admin);
            }

            if (userRepository.findByUsername("reader").isEmpty()) {
                User reader = User.builder()
                        .username("reader")
                        .email("reader@example.com")
                        .passwordHash(passwordEncoder.encode("reader123"))
                        .enabled(true)
                        .roles(Set.of("READER"))
                        .build();
                userRepository.save(reader);
            }
        };
    }
}
