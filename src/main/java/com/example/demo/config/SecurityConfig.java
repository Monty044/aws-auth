package com.example.demo.config;

import com.example.demo.service.S3LoginLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Web-security configuration:
 *  • permits “/”, “/css/**”, “/js/**”, “/images/**”
 *  • requires authentication for “/admin/**”
 *  • on a successful OAuth2 login, logs the user’s email to S3
 *  • then redirects to “/admin”
 */
@Configuration
public class SecurityConfig {

    private final S3LoginLogger loginLogger;

    // We write the constructor manually instead of using Lombok:
    public SecurityConfig(S3LoginLogger loginLogger) {
        this.loginLogger = loginLogger;
    }

    /**
     * Custom OAuth2 success handler:
     * 1) extract “email” if present (or fallback to user.getName())
     * 2) call loginLogger.logLogin(email)
     * 3) redirect to “/admin”
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                Authentication authentication) -> {

            String email = "unknown@unknown";

            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User user) {
                email = Optional.ofNullable(user.<String>getAttribute("email"))
                        .orElse(user.getName());
            }

            // Log into S3
            loginLogger.logLogin(email);

            // Redirect to /admin
            response.sendRedirect("/admin");
        };
    }

    /**
     * Main security filter chain:
     *  • allow public access to “/”, “/css/**”, “/js/**”, “/images/**”
     *  • require authentication for “/admin/**”
     *  • enable OAuth2 login with our custom success handler
     *  • and configure a logout that returns to “/”
     */
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
                        .successHandler(successHandler)
                )
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}
