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

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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

    /**
     * Variante "brute" du logout : permet d'envoyer arbitrairement un cookie de session et/ou un en-tête CSRF, ou de
     * tout omettre. Utilisée par les tests CSRF qui valident explicitement les cas de rejet (403).
     */
    protected ResponseEntity<String> logout(String sessionCookie, String xsrfCookie, String xsrfHeaderValue) {
        HttpHeaders headers = new HttpHeaders();
        String cookieHeader = joinCookies(sessionCookie, xsrfCookie);
        if (!cookieHeader.isEmpty()) {
            headers.add(HttpHeaders.COOKIE, cookieHeader);
        }
        if (xsrfHeaderValue != null) {
            headers.add("X-XSRF-TOKEN", xsrfHeaderValue);
        }
        return restTemplate.exchange("/api/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    /**
     * Variante "happy path" du logout : prend le bundle complet {@link AuthCookies} et envoie les deux cookies plus
     * l'en-tête {@code X-XSRF-TOKEN}. Représente le comportement d'un client navigateur légitime.
     */
    protected ResponseEntity<String> logout(AuthCookies cookies) {
        return logout(cookies.session(), cookies.xsrfCookie(), cookies.xsrfTokenValue());
    }

    /**
     * Login + amorce du cookie {@code XSRF-TOKEN} via un {@code GET /api/auth/me}. Le filtre CSRF est exempté sur
     * {@code /login}, donc le cookie n'est pas toujours posé par la réponse de login. Un GET subséquent garantit son
     * acquisition. Retourne le bundle prêt à être utilisé sur les requêtes mutantes.
     */
    protected AuthCookies loginAndAcquireCookies(String email, String password) {
        ResponseEntity<String> loginResponse = login(email, password);
        String session = extractSessionCookie(loginResponse);
        String xsrfCookie = extractXsrfCookie(loginResponse);
        if (xsrfCookie == null && session != null) {
            HttpHeaders h = new HttpHeaders();
            h.add(HttpHeaders.COOKIE, session);
            ResponseEntity<String> meResponse =
                    restTemplate.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(h), String.class);
            xsrfCookie = extractXsrfCookie(meResponse);
        }
        return new AuthCookies(session, xsrfCookie, extractXsrfValue(xsrfCookie));
    }

    /**
     * Déclenche un {@code GET /api/auth/me} sans session (réponse 401 attendue) uniquement pour récupérer le cookie
     * {@code XSRF-TOKEN}. Permet de tester le scénario "logout idempotent" en CSRF-compliant : un navigateur qui a
     * visité le site possède le cookie XSRF même sans être connecté.
     */
    protected String acquireXsrfCookieAnonymously() {
        ResponseEntity<String> response =
                restTemplate.exchange("/api/auth/me", HttpMethod.GET, HttpEntity.EMPTY, String.class);
        return extractXsrfCookie(response);
    }

    /**
     * Extrait la portion {@code JSESSIONID=xxx} du header {@code Set-Cookie} retourne par le serveur, prête à être
     * réinjectée dans un header {@code Cookie}.
     */
    protected String extractSessionCookie(ResponseEntity<?> response) {
        return extractCookie(response, "JSESSIONID");
    }

    /**
     * Extrait la portion {@code XSRF-TOKEN=xxx} (sans les attributs Path, SameSite, etc.). Retourne {@code null} si
     * aucun cookie XSRF n'est posé par la réponse.
     */
    protected String extractXsrfCookie(ResponseEntity<?> response) {
        return extractCookie(response, "XSRF-TOKEN");
    }

    /**
     * Extrait la valeur brute du cookie XSRF (sans le préfixe {@code XSRF-TOKEN=}). C'est cette valeur qui doit être
     * réinjectée dans l'en-tête {@code X-XSRF-TOKEN} pour que le filtre CSRF accepte la requête.
     */
    protected String extractXsrfValue(String xsrfCookie) {
        if (xsrfCookie == null) {
            return null;
        }
        return xsrfCookie.substring("XSRF-TOKEN=".length());
    }

    private String extractCookie(ResponseEntity<?> response, String cookieName) {
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookies == null) {
            return null;
        }
        String prefix = cookieName + "=";
        return setCookies.stream()
                .filter(c -> c.startsWith(prefix))
                .map(c -> c.split(";", 2)[0])
                .findFirst()
                .orElse(null);
    }

    private String joinCookies(String... cookies) {
        return Stream.of(cookies)
                .filter(Objects::nonNull)
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }

    /**
     * Bundle représentant l'état "client navigateur authentifié" : session + cookie XSRF + valeur du token CSRF
     * extraite. Tous les champs peuvent être {@code null} pour modéliser des états partiels.
     */
    protected record AuthCookies(String session, String xsrfCookie, String xsrfTokenValue) {
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
