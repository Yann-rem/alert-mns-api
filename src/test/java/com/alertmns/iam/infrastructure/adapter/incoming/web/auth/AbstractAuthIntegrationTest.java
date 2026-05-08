package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import com.alertmns.iam.application.ActivateUserService;
import com.alertmns.iam.application.RegisterUserService;
import com.alertmns.iam.application.SuspendUserService;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base commune pour les tests d'intégration de l'authentification.
 *
 * <p>Démarre un Postgres reel via Testcontainers (singleton partage par toute la JVM, jamais arrêté — le ryuk de
 * Testcontainers nettoie en fin de process), un serveur Spring Boot sur un port aléatoire, et un client HTTP
 * {@link TestRestTemplate}.</p>
 *
 * <p>On n'utilise pas {@code @Testcontainers}/{@code @Container} (lifecycle JUnit class-scoped) parce que Spring met
 * en cache l'ApplicationContext entre classes de test partageant la même config : le second run pointerait sur un
 * container déjà arrêté. Le pattern singleton garantit un container vivant tant que la JVM tourne.</p>
 *
 * <p>Le profil {@code test} désactive {@code DevSeedConfig} et {@code SwaggerSecurityConfig} (tous deux en
 * {@code @Profile("dev")}) — le test contrôle entièrement la creation des users.</p>
 *
 * <p>Le schema est recréé avant chaque suite ({@code create-drop}). Les users sont nettoyés apres chaque test pour
 * garantir l'isolation.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(AbstractAuthIntegrationTest.TestUserFactoryConfig.class)
abstract class AbstractAuthIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected TestUserFactory userFactory;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @AfterEach
    void cleanDatabase() {
        userJpaRepository.deleteAll();
    }

    // --- Helpers HTTP ---

    protected ResponseEntity<String> login(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        return restTemplate.postForEntity("/api/auth/login", new HttpEntity<>(body, headers), String.class);
    }

    protected ResponseEntity<String> loginRaw(String rawJsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity("/api/auth/login", new HttpEntity<>(rawJsonBody, headers), String.class);
    }

    protected ResponseEntity<String> getMe(String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }
        return restTemplate.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    protected ResponseEntity<String> logout(String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }
        return restTemplate.exchange("/api/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    /**
     * Extrait la portion {@code JSESSIONID=xxx} du header {@code Set-Cookie} retourne par le serveur, prête à être
     * réinjectée dans un header {@code Cookie}.
     */
    protected String extractSessionCookie(ResponseEntity<?> response) {
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        if (setCookie == null) {
            return null;
        }
        return setCookie.split(";", 2)[0];
    }

    /**
     * Configuration de test exposant le {@link TestUserFactory} comme bean.
     *
     * <p>Imported via {@code @Import} car les classes du repertoire {@code src/test} ne sont pas scannées par défaut
     * par Spring Boot.</p>
     */
    @TestConfiguration
    static class TestUserFactoryConfig {
        @Bean
        TestUserFactory testUserFactory(
                RegisterUserService registerUserService,
                ActivateUserService activateUserService,
                SuspendUserService suspendUserService
        ) {
            return new TestUserFactory(registerUserService, activateUserService, suspendUserService);
        }
    }
}
