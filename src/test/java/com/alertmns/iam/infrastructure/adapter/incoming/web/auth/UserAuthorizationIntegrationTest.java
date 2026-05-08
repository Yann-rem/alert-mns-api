package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration end-to-end couvrant les annotations {@code @PreAuthorize} de {@code UserController}.
 *
 * <p>Couvre :
 * <ul>
 *   <li>endpoints admin (activate / suspend / reactivate) — {@code hasRole('ADMIN')}</li>
 *   <li>endpoints self-or-admin (profile / absence-message) — SpEL self-check OR admin</li>
 * </ul>
 * </p>
 */
@DisplayName("@PreAuthorize on /api/users/*")
class UserAuthorizationIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.authz@alertmns.local";
    private static final String BOB_EMAIL = "bob.authz@alertmns.local";
    private static final String ALICE_EMAIL = "alice.authz@alertmns.local";
    private static final String PASSWORD = "secret123456";

    // ----------------------------------------------------------------------------------------------------------------
    // Endpoints admin : POST /api/users/{id}/activate
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    @DisplayName("Anonymous with CSRF but no session on POST /activate returns 401")
    void shouldReturn401WhenAnonymousActivatesUser() {
        UserId target = userFactory.registerPending(BOB_EMAIL, PASSWORD);
        String xsrfCookie = acquireXsrfCookieAnonymously();
        AuthCookies anonymousButCsrfReady = new AuthCookies(null, xsrfCookie, extractXsrfValue(xsrfCookie));

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/users/" + target.value() + "/activate",
                null,
                anonymousButCsrfReady
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Non-admin user on POST /activate of another user returns 403")
    void shouldReturn403WhenNonAdminActivatesUser() {
        userFactory.registerActive(BOB_EMAIL, PASSWORD);
        UserId target = userFactory.registerPending(ALICE_EMAIL, PASSWORD);
        AuthCookies bob = loginAndAcquireCookies(BOB_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/users/" + target.value() + "/activate",
                null,
                bob
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Admin on POST /activate returns 204")
    void shouldReturn204WhenAdminActivatesUser() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        UserId target = userFactory.registerPending(ALICE_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/users/" + target.value() + "/activate",
                null,
                admin
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Endpoints self-or-admin : PUT /api/users/{id}/profile
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    @DisplayName("User edits own profile returns 204")
    void shouldReturn204WhenUserEditsOwnProfile() {
        UserId bobId = userFactory.registerActive(BOB_EMAIL, PASSWORD);
        AuthCookies bob = loginAndAcquireCookies(BOB_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.PUT,
                "/api/users/" + bobId.value() + "/profile",
                "{\"firstName\":\"Bob\",\"lastName\":\"NewName\",\"avatar\":null}",
                bob
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("User attempts to edit another's profile returns 403")
    void shouldReturn403WhenUserEditsOtherProfile() {
        userFactory.registerActive(BOB_EMAIL, PASSWORD);
        UserId aliceId = userFactory.registerActive(ALICE_EMAIL, PASSWORD);
        AuthCookies bob = loginAndAcquireCookies(BOB_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.PUT,
                "/api/users/" + aliceId.value() + "/profile",
                "{\"firstName\":\"Alice\",\"lastName\":\"Hacked\",\"avatar\":null}",
                bob
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Admin edits another user's profile returns 204")
    void shouldReturn204WhenAdminEditsOtherProfile() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        UserId bobId = userFactory.registerActive(BOB_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.PUT,
                "/api/users/" + bobId.value() + "/profile",
                "{\"firstName\":\"Bob\",\"lastName\":\"AdminEdited\",\"avatar\":null}",
                admin
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
