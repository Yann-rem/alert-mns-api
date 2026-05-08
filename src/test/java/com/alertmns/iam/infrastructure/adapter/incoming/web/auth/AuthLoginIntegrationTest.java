package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration end-to-end pour {@code POST /api/auth/login}.
 *
 * <p>Couvre le happy path, l'anti-enumeration, les comptes non opérables (PENDING/SUSPENDED) et la validation du
 * body.</p>
 */
@DisplayName("POST /api/auth/login")
class AuthLoginIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String EMAIL = "alice@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @Test
    @DisplayName("Scenario 2 — credentials valides retournent 200 + cookie JSESSIONID")
    void shouldReturn200WithJSessionIdCookieWhenCredentialsAreValid() {
        userFactory.registerActive(EMAIL, PASSWORD);

        ResponseEntity<String> response = login(EMAIL, PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("JSESSIONID=");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/");
    }

    @Test
    @DisplayName("Scenario 3 — mauvais mot de passe retourne 401 + 'Invalid credentials'")
    void shouldReturn401WithInvalidCredentialsMessageWhenPasswordIsWrong() {
        userFactory.registerActive(EMAIL, PASSWORD);

        ResponseEntity<String> response = login(EMAIL, "wrongpassword");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Invalid credentials");
    }

    @Test
    @DisplayName("Scenario 4 — email inconnu retourne 401 + meme message (anti-enumeration)")
    void shouldReturn401WithSameMessageWhenEmailIsUnknown() {
        // Pas de user en base.

        ResponseEntity<String> response = login("unknown@alertmns.local", PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Invalid credentials");
    }

    @Test
    @DisplayName("Scenario 5 — compte SUSPENDED retourne 403 + 'Account is locked'")
    void shouldReturn403WhenAccountIsSuspended() {
        userFactory.registerSuspended(EMAIL, PASSWORD);

        ResponseEntity<String> response = login(EMAIL, PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("Account is locked");
    }

    @Test
    @DisplayName("Scenario 6 — compte PENDING retourne 403 + 'Account is disabled'")
    void shouldReturn403WhenAccountIsPending() {
        userFactory.registerPending(EMAIL, PASSWORD);

        ResponseEntity<String> response = login(EMAIL, PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("Account is disabled");
    }

    @Test
    @DisplayName("Scenario 10 — body invalide (email vide) retourne 400")
    void shouldReturn400WhenEmailIsBlank() {
        ResponseEntity<String> response = loginRaw("{\"email\":\"\",\"password\":\"" + PASSWORD + "\"}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
