package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration end-to-end pour {@code POST /api/auth/logout}.
 *
 * <p>Couvre l'invalidation de session (login -> logout -> me => 401) et l'idempotence (logout sans session retourne
 * quand meme 204).</p>
 */
@DisplayName("POST /api/auth/logout")
class AuthLogoutIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String EMAIL = "carol@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @Test
    @DisplayName("Scenario 8 — login puis logout invalide la session (me retourne 401 ensuite)")
    void shouldInvalidateSessionAfterLogout() {
        userFactory.registerActive(EMAIL, PASSWORD);
        ResponseEntity<String> loginResponse = login(EMAIL, PASSWORD);
        String sessionCookie = extractSessionCookie(loginResponse);
        assertThat(sessionCookie).isNotNull();

        ResponseEntity<String> logoutResponse = logout(sessionCookie);
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> meAfterLogout = getMe(sessionCookie);
        assertThat(meAfterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Scenario 9 — logout sans session retourne 204 (idempotent)")
    void shouldReturn204WhenLogoutWithoutSession() {
        ResponseEntity<String> response = logout(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
