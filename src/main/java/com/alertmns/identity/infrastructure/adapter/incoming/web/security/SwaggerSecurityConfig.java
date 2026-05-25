package com.alertmns.identity.infrastructure.adapter.incoming.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Filter chain dédié à l'exposition de Swagger UI en environnement {@code dev} uniquement.
 *
 * <p>Active uniquement avec le profil {@code dev}. En production, Swagger reste protégé par la chaîne principale
 * ({@link SecurityConfig}) et n'est donc pas accessible publiquement.</p>
 */
@Configuration
@Profile("dev")
public class SwaggerSecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/webjars/**"
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
