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
    @DisplayName("le filtre sur le groupe ne renvoie que ses membres")
    void shouldFilterByGroup() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        String groupId = createGroup("Promotion CDA", admin);
        addToGroup(groupId, memberIdOf(MEMBER_EMAIL, admin), admin);

        ResponseEntity<String> response = listMembers("?groupId=" + groupId, ADMIN_EMAIL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .as("l'appartenance est portée par GroupMembership, pas par le membre")
                .contains(MEMBER_EMAIL)
                .doesNotContain(ADMIN_EMAIL);
        assertThat(response.getBody()).contains("\"total\":1");
    }

    @Test
    @DisplayName("un groupe sans membre renvoie une page vide")
    void shouldReturnEmptyPageForGroupWithoutMembers() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        String groupId = createGroup("Groupe vide", admin);

        ResponseEntity<String> response = listMembers("?groupId=" + groupId, ADMIN_EMAIL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"total\":0").contains("\"items\":[]");
    }

    @Test
    @DisplayName("le filtre sur le groupe se combine avec la recherche textuelle")
    void shouldCombineGroupFilterWithSearch() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        String groupId = createGroup("Promotion CDA", admin);
        addToGroup(groupId, memberIdOf(MEMBER_EMAIL, admin), admin);

        // Sofia est dans le groupe, mais la recherche ne la désigne pas : les deux filtres se cumulent.
        ResponseEntity<String> response = listMembers("?groupId=" + groupId + "&q=zzz-inexistant", ADMIN_EMAIL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"total\":0");
    }

    /** @return l'identifiant du groupe créé */
    private String createGroup(String name, AuthCookies admin) {
        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/organisations/" + ORG + "/groups",
                "{\"name\":\"" + name + "\"}",
                admin);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // La réponse de création expose « id », là où la liste des groupes expose « groupId ».
        return extractJsonValue(response.getBody(), "id");
    }

    private void addToGroup(String groupId, String memberId, AuthCookies admin) {
        ResponseEntity<String> response = mutate(
                HttpMethod.PUT,
                "/api/organisations/" + ORG + "/groups/" + groupId + "/members/" + memberId,
                null,
                admin);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    /** Retrouve le {@code memberId} d'un compte via la liste du backoffice. */
    private String memberIdOf(String email, AuthCookies admin) {
        ResponseEntity<String> response = listMembers("?q=" + email, ADMIN_EMAIL);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return extractJsonValue(response.getBody(), "memberId");
    }

    /**
     * Extraction volontairement rustique : le corps testé ne contient qu'une occurrence de la clé.
     *
     * <p>Échoue explicitement si la clé est absente. Un retour silencieux produirait un
     * identifiant illisible, et donc une liste vide : le test passerait pour la mauvaise raison.</p>
     */
    private static String extractJsonValue(String body, String key) {
        String marker = "\"" + key + "\":\"";
        int start = body.indexOf(marker);
        assertThat(start).as("clé « %s » absente de la réponse : %s", key, body).isNotNegative();
        int valueStart = start + marker.length();
        return body.substring(valueStart, body.indexOf('"', valueStart));
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
