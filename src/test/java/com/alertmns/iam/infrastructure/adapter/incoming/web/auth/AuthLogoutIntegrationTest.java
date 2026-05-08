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
        AuthCookies cookies = loginAndAcquireCookies(EMAIL, PASSWORD);
        assertThat(cookies.session()).isNotNull();
        assertThat(cookies.xsrfTokenValue()).isNotNull();

        ResponseEntity<String> logoutResponse = logout(cookies);
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> meAfterLogout = getMe(cookies.session());
        assertThat(meAfterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Scenario 9 — logout sans session mais avec CSRF retourne 204 (idempotent)")
    void shouldReturn204WhenLogoutWithoutSession() {
        String xsrfCookie = acquireXsrfCookieAnonymously();
        assertThat(xsrfCookie).isNotNull();

        ResponseEntity<String> response = logout(null, xsrfCookie, extractXsrfValue(xsrfCookie));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
