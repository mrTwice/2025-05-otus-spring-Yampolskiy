package ru.otus.hw.users.repository;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.users.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsernameOrEmail(String username, String email);

    @EntityGraph(attributePaths = "roles")
    @NonNull
    Optional<User> findById(@NonNull Long id);

    @EntityGraph(attributePaths = "roles")
    @NonNull
    List<User> findAll();

    @EntityGraph(attributePaths = "roles")
    @NonNull
    List<User> findAll(@NonNull Sort sort);

    @EntityGraph(attributePaths = "roles")
    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}