package ru.otus.hw.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.security.model.AppUserDetails;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.service.UserReadService;


@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserReadService userReadService;

    @Override
    public UserDetails loadUserByUsername(String nameOrEmail) {
        try {
            User user = userReadService.getByUsernameOrEmail(nameOrEmail);
            return new AppUserDetails(user);
        } catch (NotFoundException notFoundException) {
            throw new UsernameNotFoundException("User not found: " + nameOrEmail, notFoundException);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException("Authentication backend error", ex);
        }
    }
}
