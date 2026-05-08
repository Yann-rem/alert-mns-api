package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration end-to-end couvrant la protection CSRF sur les endpoints d'authentification.
 *
 * <p>Couvre :
 * <ul>
 *   <li>présence et attributs du cookie {@code XSRF-TOKEN} (lisible par le JS, eager loading)</li>
 *   <li>rejet 403 sur {@code POST /api/auth/logout} sans en-tête {@code X-XSRF-TOKEN}</li>
 *   <li>rejet 403 sur {@code POST /api/auth/logout} avec en-tête de mauvaise valeur</li>
 *   <li>exemption du filtre CSRF sur {@code POST /api/auth/login}</li>
 * </ul>
 * </p>
 *
 * <p>Stratégie retenue : double-submit cookie via {@code CookieCsrfTokenRepository.withHttpOnlyFalse()} — voir
 * {@code docs/notes/csrf-double-submit-cookie.md}.</p>
 */
@DisplayName("CSRF protection")
class AuthCsrfIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String EMAIL = "dave@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @Test
    @DisplayName("Scenario CSRF-1 — cookie XSRF-TOKEN posé sur le premier GET et lisible par le JS")
    void shouldSetXsrfCookieReadableByJavascript() {
        ResponseEntity<String> response =
                restTemplate.exchange("/api/auth/me", HttpMethod.GET, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).isNotNull();
        String xsrfRaw = setCookies.stream()
                .filter(c -> c.startsWith("XSRF-TOKEN="))
                .findFirst()
                .orElse(null);
        assertThat(xsrfRaw)
                .as("Le cookie XSRF-TOKEN doit être posé dès le premier GET (eager loading)")
                .isNotNull();
        assertThat(xsrfRaw).doesNotContainIgnoringCase("HttpOnly");
        assertThat(xsrfRaw).contains("Path=/");
    }

    @Test
    @DisplayName("Scenario CSRF-2 — POST /logout sans header X-XSRF-TOKEN retourne 403")
    void shouldRejectLogoutWithoutCsrfHeader() {
        userFactory.registerActive(EMAIL, PASSWORD);
        AuthCookies cookies = loginAndAcquireCookies(EMAIL, PASSWORD);
        assertThat(cookies.session()).isNotNull();
        assertThat(cookies.xsrfCookie()).isNotNull();

        ResponseEntity<String> response = logout(cookies.session(), cookies.xsrfCookie(), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Scenario CSRF-3 — POST /logout avec header X-XSRF-TOKEN différent du cookie retourne 403")
    void shouldRejectLogoutWithMismatchedCsrfHeader() {
        userFactory.registerActive(EMAIL, PASSWORD);
        AuthCookies cookies = loginAndAcquireCookies(EMAIL, PASSWORD);

        ResponseEntity<String> response =
                logout(cookies.session(), cookies.xsrfCookie(), "this-is-not-the-real-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Scenario CSRF-4 — POST /login fonctionne sans CSRF (endpoint exempté)")
    void shouldAllowLoginWithoutCsrfToken() {
        userFactory.registerActive(EMAIL, PASSWORD);

        ResponseEntity<String> response = login(EMAIL, PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
