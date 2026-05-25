package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

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

    /**
     * Note : ce test prouve aussi implicitement l'exemption CSRF sur {@code /api/auth/login} — le helper
     * {@code login(...)} envoie un POST sans header {@code X-XSRF-TOKEN}, donc un succès ici garantit l'exemption.
     */
    @Test
    @DisplayName("Scenario 2 — valid credentials return 200 + JSESSIONID cookie")
    void shouldReturn200WithJSessionIdCookieWhenCredentialsAreValid() {
        userFactory.registerActive(EMAIL, PASSWORD);

        ResponseEntity<String> response = login(EMAIL, PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).isNotNull();
        String jsessionId = setCookies.stream()
                .filter(c -> c.startsWith("JSESSIONID="))
                .findFirst()
                .orElse(null);
        assertThat(jsessionId).isNotNull();
        assertThat(jsessionId).contains("HttpOnly");
        assertThat(jsessionId).contains("Path=/");
    }

    @Test
    @DisplayName("Scenario 3 — wrong password returns 401 + 'Invalid credentials'")
    void shouldReturn401WithInvalidCredentialsMessageWhenPasswordIsWrong() {
        userFactory.registerActive(EMAIL, PASSWORD);

        ResponseEntity<String> response = login(EMAIL, "wrongpassword");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Invalid credentials");
    }

    @Test
    @DisplayName("Scenario 4 — unknown email returns 401 + same message (anti-enumeration)")
    void shouldReturn401WithSameMessageWhenEmailIsUnknown() {
        ResponseEntity<String> response = login("unknown@alertmns.local", PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Invalid credentials");
    }

    @Test
    @DisplayName("Scenario 5 — SUSPENDED account returns 403 + 'Account is locked'")
    void shouldReturn403WhenAccountIsSuspended() {
        userFactory.registerSuspended(EMAIL, PASSWORD);

        ResponseEntity<String> response = login(EMAIL, PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("Account is locked");
    }

    @Test
    @DisplayName("Scenario 6 — PENDING account returns 403 + 'Account is disabled'")
    void shouldReturn403WhenAccountIsPending() {
        userFactory.registerPending(EMAIL);

        ResponseEntity<String> response = login(EMAIL, PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("Account is disabled");
    }

    @Test
    @DisplayName("Scenario 10 — invalid body (blank email) returns 400")
    void shouldReturn400WhenEmailIsBlank() {
        ResponseEntity<String> response = loginRaw("{\"email\":\"\",\"password\":\"" + PASSWORD + "\"}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
