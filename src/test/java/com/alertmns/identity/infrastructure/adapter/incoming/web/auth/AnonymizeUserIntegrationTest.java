package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration end-to-end de l'endpoint {@code POST /api/users/{id}/anonymize} (droit à l'effacement RGPD).
 *
 * <p>Couvre à la fois l'autorisation ({@code @PreAuthorize("hasRole('ADMIN')")}) et l'effet métier : les PII de
 * l'utilisateur ciblé sont effacées en base tandis que son {@code userId} est conservé.</p>
 */
@DisplayName("POST /api/users/{id}/anonymize")
class AnonymizeUserIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.anon@alertmns.local";
    private static final String BOB_EMAIL = "bob.anon@alertmns.local";
    private static final String ALICE_EMAIL = "alice.anon@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Admin anonymizes a user: returns 204 and scrubs the PII while keeping the userId")
    void shouldReturn204AndScrubPiiWhenAdminAnonymizes() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        UserId bobId = userFactory.registerActive(BOB_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/users/" + bobId.value() + "/anonymize",
                null,
                admin
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        User bob = userRepository.findById(bobId).orElseThrow();
        assertThat(bob.id()).isEqualTo(bobId);
        assertThat(bob.isAnonymized()).isTrue();
        assertThat(bob.email().value()).isEqualTo("anonymized-" + bobId.value() + "@deleted.local");
        assertThat(bob.profile().firstName().value()).isEqualTo("Utilisateur");
        assertThat(bob.profile().lastName().value()).isEqualTo("Supprimé");
    }

    @Test
    @DisplayName("Non-admin attempts to anonymize another user: returns 403 and leaves the target untouched")
    void shouldReturn403WhenNonAdminAnonymizesOther() {
        UserId aliceId = userFactory.registerActive(ALICE_EMAIL, PASSWORD);
        userFactory.registerActive(BOB_EMAIL, PASSWORD);
        AuthCookies bob = loginAndAcquireCookies(BOB_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/users/" + aliceId.value() + "/anonymize",
                null,
                bob
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(userRepository.findById(aliceId).orElseThrow().isAnonymized()).isFalse();
    }

    @Test
    @DisplayName("Non-admin attempts to anonymize themselves: returns 403 (endpoint is admin-only)")
    void shouldReturn403WhenNonAdminAnonymizesSelf() {
        UserId bobId = userFactory.registerActive(BOB_EMAIL, PASSWORD);
        AuthCookies bob = loginAndAcquireCookies(BOB_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/users/" + bobId.value() + "/anonymize",
                null,
                bob
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(userRepository.findById(bobId).orElseThrow().isAnonymized()).isFalse();
    }
}
