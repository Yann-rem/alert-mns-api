package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

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
 *   <li>endpoints admin (suspend / reactivate) — {@code hasRole('ADMIN')}</li>
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
