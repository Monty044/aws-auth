package com.example.demo.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Optional;

/**
 * Web-security configuration:
 *  • permits static assets + “/”
 *  • protects “/admin/**”
 *  • logs every successful OAuth2 login to S3
 *  • redirects the user to /admin after login
 */
@ConfigurationS
@RequiredArgsConstructor   // injects the final field below
public class SecurityConfig {

    private final S3LoginLogger loginLogger;   // your logger bean

    /** Custom success-handler that appends a line to today’s S3 log file */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                Authentication authentication) -> {

            String email = "unknown@unknown";

            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User user) {
                // first try the “email” claim, otherwise use the OAuth2 “name”
                email = Optional.ofNullable(user.<String>getAttribute("email"))
                        .orElse(user.getName());
            }

            loginLogger.logLogin(email);   // ← write to S3
            response.sendRedirect("/admin");
        };
    }

    /** Main Spring-Security DSL */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationSuccessHandler successHandler)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .successHandler(successHandler)   // plug in the custom logger
                )
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}
