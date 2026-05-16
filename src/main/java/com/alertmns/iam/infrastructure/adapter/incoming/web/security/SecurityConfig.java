package com.alertmns.iam.infrastructure.adapter.incoming.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> {
                    CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
                    handler.setCsrfRequestAttributeName(null);
                    csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                            .csrfTokenRequestHandler(handler)
                            .ignoringRequestMatchers("/api/auth/login", "/api/auth/magic-link/redeem");
                })

                // Pas de form-login HTML, pas de Basic Auth : on aura un endpoint JSON custom
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Session cookie httpOnly (défaut Spring Boot) — on créera une session au login
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // Sur ressource non authentifiée : 401 JSON, pas de redirection
                .exceptionHandling(eh ->
                        eh.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // Règles d'autorisation
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/magic-link/validate", "/api/auth/magic-link/redeem").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}
