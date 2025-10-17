package ru.otus.hw.users.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.otus.hw.library.components.GlobalExceptionHandler;
import ru.otus.hw.security.config.SecurityConfig;
import ru.otus.hw.security.model.AppUserDetails;
import ru.otus.hw.security.service.SessionKiller;
import ru.otus.hw.users.model.User;
import ru.otus.hw.users.service.AccountPasswordService;
import ru.otus.hw.users.service.UserAccountService;
import ru.otus.hw.users.service.UserReadService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProfileController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class
        )
)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserReadService userReadService;

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private AccountPasswordService accountPasswordService;

    @MockitoBean
    private SessionKiller sessionKiller;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private AppUserDetails appUser;

    @BeforeEach
    void setUp() {
        appUser = Mockito.mock(AppUserDetails.class, Mockito.withSettings().lenient());
        Mockito.when(appUser.getId()).thenReturn(42L);
        Mockito.when(appUser.getUsername()).thenReturn("user");
        Mockito.when(appUser.getPassword()).thenReturn("pwd");

        List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Mockito.<Collection<? extends GrantedAuthority>>when(appUser.getAuthorities())
                .thenReturn(auths);

        Mockito.when(appUser.isAccountNonExpired()).thenReturn(true);
        Mockito.when(appUser.isAccountNonLocked()).thenReturn(true);
        Mockito.when(appUser.isCredentialsNonExpired()).thenReturn(true);
        Mockito.when(appUser.isEnabled()).thenReturn(true);

        Mockito.when(userDetailsService.loadUserByUsername("user")).thenReturn(appUser);
    }

    private static ResultMatcher redirectsToLogin() {
        return ResultMatcher.matchAll(
                status().is3xxRedirection(),
                redirectedUrlPattern("**/login")
        );
    }


    @Test
    @WithAnonymousUser
    void anon_view_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(redirectsToLogin());
    }

    @Test
    @WithAnonymousUser
    void anon_editForm_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/profile/edit"))
                .andExpect(redirectsToLogin());
    }

    @Test
    @WithAnonymousUser
    void anon_update_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/profile")
                        .param("username", "newname")
                        .param("email", "new@mail.com")
                        .with(csrf()))
                .andExpect(redirectsToLogin());
    }

    @Test
    @WithAnonymousUser
    void anon_changePassword_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/profile/password")
                        .param("currentPassword", "old")
                        .param("newPassword", "newStrong1!")
                        .param("confirmPassword", "newStrong1!")
                        .with(csrf()))
                .andExpect(redirectsToLogin());
    }


    @Test
    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void view_ok_forAuthenticated() throws Exception {
        var domainUser = User.builder()
                .id(42L)
                .username("user")
                .email("user@mail.com")
                .passwordHash("secret")
                .enabled(true)
                .roles(Set.of("ROLE_USER"))
                .build();

        Mockito.when(userReadService.getById(42L)).thenReturn(domainUser);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editForm_ok_forAuthenticated() throws Exception {
        var domainUser = User.builder()
                .id(42L)
                .username("user")
                .email("user@mail.com")
                .passwordHash("secret")
                .enabled(true)
                .roles(Set.of("ROLE_USER"))
                .build();

        Mockito.when(userReadService.getById(42L)).thenReturn(domainUser);

        mockMvc.perform(get("/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile-edit"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void update_ok_forAuthenticated() throws Exception {
        mockMvc.perform(post("/profile")
                        .param("username", "newname")
                        .param("email", "new@mail.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?updated"));

        Mockito.verify(userAccountService)
                .updateProfile(42L, "newname", "new@mail.com");
    }

    @Test
    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changePassword_ok_forAuthenticated() throws Exception {
        mockMvc.perform(post("/profile/password")
                        .param("currentPassword", "old")
                        .param("newPassword", "newStrong1!")
                        .param("confirmPassword", "newStrong1!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?pwdChanged"));

        Mockito.verify(accountPasswordService)
                .changePassword(eq(42L), eq("old"), eq("newStrong1!"), eq("newStrong1!"));
        Mockito.verify(sessionKiller)
                .expireOnPasswordChange(eq("user"), any(HttpServletRequest.class),
                        any(HttpServletResponse.class), any());
    }

    @Test
    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void update_validationError_returnsEditView() throws Exception {
        mockMvc.perform(post("/profile")
                        .param("username", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile-edit"));

        Mockito.verifyNoInteractions(userAccountService);
    }

    @Test
    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changePassword_validationError_returnsProfileView() throws Exception {
        var domainUser = User.builder()
                .id(42L).username("user").email("user@mail.com")
                .passwordHash("x").enabled(true).roles(Set.of("ROLE_USER"))
                .build();
        Mockito.when(userReadService.getById(42L)).thenReturn(domainUser);

        mockMvc.perform(post("/profile/password")
                        .param("currentPassword", "old")
                        .param("newPassword", "newStrong1!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"));

        Mockito.verifyNoInteractions(accountPasswordService, sessionKiller);
    }
}
