package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.identity.domain.port.outgoing.MailerPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MembershipInvitationJpaRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Invitation d'un membre et lecture des invitations en attente (ADR-0014).
 */
@DisplayName("POST /members et GET /invitations")
class InviteMemberIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ORG = TestUserFactory.DEFAULT_ORGANISATION_ID;
    private static final String ADMIN_EMAIL = "admin.invite@alertmns.local";
    private static final String MEMBER_EMAIL = "membre.invite@alertmns.local";
    private static final String INVITEE_EMAIL = "karim.invite@alertmns.local";
    private static final String PASSWORD = "invitepassword1234";

    @MockitoBean
    private MailerPort mailer;

    @Autowired
    private MembershipInvitationJpaRepository invitationJpaRepository;

    @AfterEach
    void cleanInvitations() {
        invitationJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("un ADMIN invite : l'invité apparaît dans les invitations en attente, pas dans les membres")
    void shouldInviteAndExposeAsPendingInvitation() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        ResponseEntity<String> created = mutate(
                HttpMethod.POST,
                "/api/organisations/" + ORG + "/members",
                body(INVITEE_EMAIL, "MEMBER"),
                admin);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> invitations = get("/api/organisations/" + ORG + "/invitations", admin);
        assertThat(invitations.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(invitations.getBody())
                .contains(INVITEE_EMAIL)
                .contains("\"firstName\":\"Karim\"")
                .contains("\"role\":\"MEMBER\"")
                .contains("\"expired\":false");

        // L'invité n'est pas encore un Member : il ne doit pas polluer la liste des membres.
        ResponseEntity<String> members = get("/api/organisations/" + ORG + "/members", admin);
        assertThat(members.getBody()).doesNotContain(INVITEE_EMAIL);
    }

    @Test
    @DisplayName("inviter deux fois le même e-mail renvoie 409")
    void shouldRejectDuplicateInvitation() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);
        String path = "/api/organisations/" + ORG + "/members";

        assertThat(mutate(HttpMethod.POST, path, body(INVITEE_EMAIL, "MEMBER"), admin).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> duplicate =
                mutate(HttpMethod.POST, path, body(INVITEE_EMAIL, "MEMBER"), admin);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("une requête invalide renvoie 400")
    void shouldRejectInvalidRequest() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/organisations/" + ORG + "/members",
                body("pas-un-email", "MEMBER"),
                admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("un membre non-ADMIN ne peut pas inviter")
    void shouldRejectNonAdminInvite() {
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);
        AuthCookies member = loginAndAcquireCookies(MEMBER_EMAIL, PASSWORD);

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/organisations/" + ORG + "/members",
                body(INVITEE_EMAIL, "MEMBER"),
                member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private static String body(String email, String role) {
        return """
                {"email":"%s","firstName":"Karim","lastName":"Belkacem","role":"%s"}"""
                .formatted(email, role);
    }

    private ResponseEntity<String> get(String path, AuthCookies cookies) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookies.session());
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }
}
