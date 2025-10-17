package ru.otus.hw.users.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.ResultMatcher;
import ru.otus.hw.library.components.GlobalExceptionHandler;
import ru.otus.hw.security.config.SecurityConfig;
import ru.otus.hw.security.model.AppUserDetails;
import ru.otus.hw.users.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyBoolean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import ru.otus.hw.users.service.UserAdminService;
import ru.otus.hw.users.service.UserReadService;

@WebMvcTest(
        controllers = UserAdminController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class
        )
)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserReadService userReadService;

    @MockitoBean
    private UserAdminService userAdminService;

    private AppUserDetails adminPrincipal;
    private AppUserDetails userPrincipal;

    @BeforeEach
    void setUp() {
        adminPrincipal = mockPrincipal(1L, "admin", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        userPrincipal  = mockPrincipal(2L, "user",  List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private AppUserDetails mockPrincipal(Long id, String username, List<GrantedAuthority> authorities) {
        AppUserDetails p = Mockito.mock(AppUserDetails.class, Mockito.withSettings().lenient());
        when(p.getId()).thenReturn(id);
        when(p.getUsername()).thenReturn(username);
        when(p.getPassword()).thenReturn("x");
        Mockito.<Collection<? extends GrantedAuthority>>when(p.getAuthorities())
                .thenReturn(authorities);
        when(p.isAccountNonExpired()).thenReturn(true);
        when(p.isAccountNonLocked()).thenReturn(true);
        when(p.isCredentialsNonExpired()).thenReturn(true);
        when(p.isEnabled()).thenReturn(true);
        return p;
    }

    private static User userEntity(long id, String name, String email) {
        return User.builder()
                .id(id)
                .username(name)
                .email(email)
                .passwordHash("ph")
                .enabled(true)
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    private static ResultMatcher redirectsToLogin() {
        return ResultMatcher.matchAll(status().is3xxRedirection(), redirectedUrlPattern("**/login"));
    }


    @Test @WithAnonymousUser
    void anon_list_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/users")).andExpect(redirectsToLogin());
    }

    @Test @WithAnonymousUser
    void anon_post_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/users/10/enable").param("enabled", "true").with(csrf()))
                .andExpect(redirectsToLogin());
    }


    @Test
    void user_forbidden_on_list() throws Exception {
        mockMvc.perform(get("/users").with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void user_forbidden_on_post() throws Exception {
        mockMvc.perform(post("/users/10/enable").with(user(userPrincipal)).with(csrf())
                        .param("enabled", "true"))
                .andExpect(status().isForbidden());
    }


    @Test
    void admin_list_ok() throws Exception {
        when(userReadService.getPage(PageRequest.of(0, 1000)))
                .thenReturn(new PageImpl<>(List.of(
                        userEntity(10, "u1", "u1@ex"),
                        userEntity(11, "u2", "u2@ex")
                )));

        mockMvc.perform(get("/users").with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/list"))
                .andExpect(model().attributeExists("users"));
    }


    @Test
    void admin_enable_user_ok() throws Exception {
        mockMvc.perform(post("/users/10/enable").with(user(adminPrincipal)).with(csrf())
                        .param("enabled", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userAdminService).setEnabled(10L, true);
    }

    @Test
    void admin_self_disable_forbidden_branch() throws Exception {
        mockMvc.perform(post("/users/1/enable").with(user(adminPrincipal)).with(csrf())
                        .param("enabled", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users?error=selfDisableForbidden"));
        verify(userAdminService, never()).setEnabled(anyLong(), anyBoolean());
    }

    @Test
    void admin_addRole_ok() throws Exception {
        mockMvc.perform(post("/users/10/roles/add").with(user(adminPrincipal)).with(csrf())
                        .param("role", "MANAGER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userAdminService).addRole(10L, "MANAGER");
    }

    @Test
    void admin_removeRole_ok() throws Exception {
        mockMvc.perform(post("/users/10/roles/remove").with(user(adminPrincipal)).with(csrf())
                        .param("role", "MANAGER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userAdminService).removeRole(10L, "MANAGER");
    }

    @Test
    void admin_self_remove_admin_role_forbidden_branch() throws Exception {
        mockMvc.perform(post("/users/1/roles/remove").with(user(adminPrincipal)).with(csrf())
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users?error=selfAdminRemovalForbidden"));
        verify(userAdminService, never()).removeRole(anyLong(), anyString());
    }

    @Test
    void admin_delete_user_ok() throws Exception {
        mockMvc.perform(post("/users/10/delete").with(user(adminPrincipal)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userAdminService).delete(10L);
    }

    @Test
    void admin_self_delete_forbidden_branch() throws Exception {
        mockMvc.perform(post("/users/1/delete").with(user(adminPrincipal)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users?error=selfDeleteForbidden"));
        verify(userAdminService, never()).delete(anyLong());
    }
}
