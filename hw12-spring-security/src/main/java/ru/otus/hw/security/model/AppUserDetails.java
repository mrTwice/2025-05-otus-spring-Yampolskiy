package ru.otus.hw.security.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.otus.hw.users.model.User;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AppUserDetails implements UserDetails {

    @Getter
    private final Long id;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final Set<String> roles;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.enabled = user.isEnabled();
        this.roles = user.getRoles();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles == null ? Set.of() :
                roles.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
