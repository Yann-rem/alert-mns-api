package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration end-to-end pour {@code GET /api/auth/me}.
 *
 * <p>Couvre l'absence de session (401) et le workflow complet login -> me (200 + JSON).</p>
 */
@DisplayName("GET /api/auth/me")
class AuthMeIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String EMAIL = "bob@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @Test
    @DisplayName("Scenario 1 — absence de session retourne 401")
    void shouldReturn401WhenNoSession() {
        ResponseEntity<String> response = getMe(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Scenario 7 — login puis me retourne 200 + JSON identite")
    void shouldReturn200WithIdentityWhenAuthenticated() {
        userFactory.registerActive(EMAIL, PASSWORD);
        ResponseEntity<String> loginResponse = login(EMAIL, PASSWORD);
        String sessionCookie = extractSessionCookie(loginResponse);
        assertThat(sessionCookie).isNotNull();

        ResponseEntity<String> response = getMe(sessionCookie);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).contains("\"email\":\"" + EMAIL + "\"");
        assertThat(body).contains("\"firstName\":\"Test\"");
        assertThat(body).contains("\"lastName\":\"User\"");
        assertThat(body).contains("\"role\":\"USER\"");
        assertThat(body).contains("\"userId\":");
        assertThat(body).contains("\"organisationId\":");
    }
}
