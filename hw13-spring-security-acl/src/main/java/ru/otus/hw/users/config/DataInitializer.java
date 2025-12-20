package ru.otus.hw.users.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.library.models.Author;
import ru.otus.hw.library.models.Book;
import ru.otus.hw.library.models.Genre;
import ru.otus.hw.library.repositories.AuthorRepository;
import ru.otus.hw.library.repositories.BookRepository;
import ru.otus.hw.library.repositories.CommentRepository;
import ru.otus.hw.library.repositories.GenreRepository;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.repository.UserRepository;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final GenreRepository genreRepository;

    private final AuthorRepository authorRepository;

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    public CommandLineRunner initData() {
        return args -> {
            initAdminUser();
            initReaderUser();
            initGenresAndAuthorsAndBooks();
        };
    }

    private void initAdminUser() {
        String adminRawPassword = "admin";
        LOGGER.info("adminRawPassword: {}", adminRawPassword);

        userRepository.findByUsername("admin").orElseGet(() -> {
            User u = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .passwordHash(passwordEncoder.encode(adminRawPassword))
                    .enabled(true)
                    .roles(Set.of("ADMIN", "READER"))
                    .build();
            return userRepository.save(u);
        });
    }

    private void initReaderUser() {
        String userRawPassword = "user";
        LOGGER.info("userRawPassword: {}", userRawPassword);

        userRepository.findByUsername("user").orElseGet(() -> {
            User u = User.builder()
                    .username("user")
                    .email("reader@example.com")
                    .passwordHash(passwordEncoder.encode(userRawPassword))
                    .enabled(true)
                    .roles(Set.of("READER"))
                    .build();
            return userRepository.save(u);
        });
    }

    private void initGenresAndAuthorsAndBooks() {
        Genre g1 = ensureGenre("Fantasy");
        Genre g2 = ensureGenre("Science Fiction");
        Genre g3 = ensureGenre("Drama");

        Author a1 = ensureAuthor("Author One");
        Author a2 = ensureAuthor("Author Two");
        Author a3 = ensureAuthor("Author Three");

        ensureBook("The First Book", a1, Set.of(g1, g2));
        ensureBook("The Second Book", a2, Set.of(g3));
        ensureBook("The Third Book", a3, Set.of(g1));
    }


    private Genre ensureGenre(String name) {
        return genreRepository.findByName(name)
                .orElseGet(() -> genreRepository.save(new Genre(null, name, 0L)));
    }

    private Author ensureAuthor(String fullName) {
        return authorRepository.findByFullName(fullName)
                .orElseGet(() -> authorRepository.save(new Author(null, fullName, 0L)));
    }

    private Book ensureBook(String title, Author author, Set<Genre> genres) {
        return bookRepository.findByAuthorAndTitle(author, title)
                .orElseGet(() -> {
                    Book b = new Book();
                    b.setTitle(title);
                    b.setAuthor(author);
                    b.setVersion(0L);
                    b.setGenres(genres);
                    return bookRepository.save(b);
                });
    }
}
