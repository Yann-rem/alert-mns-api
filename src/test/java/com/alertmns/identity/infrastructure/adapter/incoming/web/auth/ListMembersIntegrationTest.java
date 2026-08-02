package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Endpoint de lecture du backoffice (ADR-0014). Placé dans ce package car il réutilise
 * {@link AbstractAuthIntegrationTest}, comme les autres tests d'autorisation inter-BC.
 */
@DisplayName("GET /api/organisations/{orgId}/members")
class ListMembersIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ORG = TestUserFactory.DEFAULT_ORGANISATION_ID;
    private static final String ADMIN_EMAIL = "admin.list@alertmns.local";
    private static final String MEMBER_EMAIL = "sofia.list@alertmns.local";
    private static final String PASSWORD = "listpassword1234";

    @Test
    @DisplayName("un ADMIN obtient les membres enrichis de leur identité")
    void shouldListMembersEnrichedWithIdentity() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);

        ResponseEntity<String> response = listMembers("", ADMIN_EMAIL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .as("l'e-mail provient du BC Identity, le rôle du BC Organisation")
                .contains(ADMIN_EMAIL)
                .contains(MEMBER_EMAIL)
                .contains("\"role\":\"ADMIN\"")
                .contains("\"memberStatus\":\"ACTIVE\"")
                .contains("\"accountStatus\":\"ACTIVE\"");
        assertThat(response.getBody()).contains("\"total\":2");
    }

    @Test
    @DisplayName("le filtre sur le rôle restreint la liste")
    void shouldFilterByRole() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);

        ResponseEntity<String> response = listMembers("?role=ADMIN", ADMIN_EMAIL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(ADMIN_EMAIL).doesNotContain(MEMBER_EMAIL);
        assertThat(response.getBody()).contains("\"total\":1");
    }

    @Test
    @DisplayName("la recherche textuelle porte sur des champs du BC Identity")
    void shouldSearchOnIdentityFields() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);

        ResponseEntity<String> response = listMembers("?q=sofia", ADMIN_EMAIL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(MEMBER_EMAIL).doesNotContain(ADMIN_EMAIL);
    }

    @Test
    @DisplayName("une recherche sans résultat renvoie une page vide")
    void shouldReturnEmptyPageWhenSearchMatchesNobody() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);

        ResponseEntity<String> response = listMembers("?q=zzz-inexistant", ADMIN_EMAIL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"total\":0").contains("\"items\":[]");
    }

    @Test
    @DisplayName("un membre non-ADMIN reçoit 403")
    void shouldRejectNonAdmin() {
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);

        ResponseEntity<String> response = listMembers("", MEMBER_EMAIL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("un appel anonyme reçoit 401")
    void shouldRejectAnonymous() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/organisations/" + ORG + "/members", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /** GET authentifié : la session suffit, aucune protection CSRF sur les lectures. */
    private ResponseEntity<String> listMembers(String queryString, String asEmail) {
        AuthCookies cookies = loginAndAcquireCookies(asEmail, PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookies.session());

        return restTemplate.exchange(
                "/api/organisations/" + ORG + "/members" + queryString,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
    }
}
