package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration end-to-end couvrant l'annotation {@code @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")}
 * de {@code AlertController} (diffusion d'alerte).
 *
 * <p>Contrat vérifié : un membre standard (ROLE_MEMBER) est refusé (403), tandis qu'un ADMIN et un MANAGER peuvent
 * diffuser (201). C'est ici que le rôle MANAGER gagne son comportement.</p>
 *
 * <p>Co-localisé dans ce package pour hériter du harnais {@link AbstractAuthIntegrationTest} (package-private).</p>
 */
@DisplayName("@PreAuthorize on /api/alerting/alerts")
class AlertAuthorizationIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.alert.authz@alertmns.local";
    private static final String MANAGER_EMAIL = "manager.alert.authz@alertmns.local";
    private static final String MEMBER_EMAIL = "member.alert.authz@alertmns.local";
    private static final String PASSWORD = "secret123456";

    private static final String BODY =
            "{\"content\":\"Alerte de test\",\"level\":\"URGENT\",\"audienceKind\":\"ORGANISATION\",\"groupId\":null}";

    @Test
    @DisplayName("Member broadcasting an alert returns 403")
    void memberForbidden() {
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);
        AuthCookies member = loginAndAcquireCookies(MEMBER_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(HttpMethod.POST, "/api/alerting/alerts", BODY, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Admin broadcasting an alert returns 201")
    void adminCreated() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(HttpMethod.POST, "/api/alerting/alerts", BODY, admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Manager broadcasting an alert returns 201")
    void managerCreated() {
        userFactory.registerActiveManager(MANAGER_EMAIL, PASSWORD);
        AuthCookies manager = loginAndAcquireCookies(MANAGER_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(HttpMethod.POST, "/api/alerting/alerts", BODY, manager);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Member listing alerts returns 200 (read is open to any member)")
    void memberListAlertsOk() {
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);
        AuthCookies member = loginAndAcquireCookies(MEMBER_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(HttpMethod.GET, "/api/alerting/alerts", null, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
