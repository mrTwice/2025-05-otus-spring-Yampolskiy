package ru.otus.hw.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**","/js/**","/images/**","/login","/register","/h2-console/**").permitAll()
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/profile/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(f -> f.loginPage("/login").permitAll())
                .logout(l -> l.logoutSuccessUrl("/login?logout").permitAll());
        return http.build();
    }
}
