package ru.otus.hw.security.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionKiller {

    private final SessionRegistry sessionRegistry;

    public void expireOnPasswordChange(
            String username,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String currentSessionId = currentSessionId(request);
        expireAllExcept(username, currentSessionId);
        new CompositeLogoutHandler(
                new SecurityContextLogoutHandler(),
                new CookieClearingLogoutHandler("JSESSIONID", "remember-me")
        ).logout(request, response, authentication);
    }

    public int expireAll(String username) {
        return expireAllExcept(username, null);
    }

    public int expireAllExcept(String username, @Nullable String exceptSessionId) {
        return findUserPrincipal(username)
                .map(p -> {
                    var sessions = sessionRegistry.getAllSessions(p, false);
                    int killed = 0;
                    for (SessionInformation si : sessions) {
                        if (exceptSessionId == null || !Objects.equals(si.getSessionId(), exceptSessionId)) {
                            si.expireNow();
                            killed++;
                        }
                    }
                    return killed;
                })
                .orElse(0);
    }

    public boolean expireBySessionId(String sessionId) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            for (SessionInformation si : sessionRegistry.getAllSessions(principal, false)) {
                if (Objects.equals(si.getSessionId(), sessionId)) {
                    si.expireNow();
                    return true;
                }
            }
        }
        return false;
    }

    private @Nullable String currentSessionId(HttpServletRequest request) {
        var sess = request.getSession(false);
        return (sess != null) ? sess.getId() : null;
    }

    private Optional<Object> findUserPrincipal(String username) {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(p -> (p instanceof UserDetails ud) && ud.getUsername().equals(username))
                .findFirst();
    }
}

