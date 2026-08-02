package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.OrganisationJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Endpoints de lecture du lot 2 (ADR-0014) : groupes et détail d'organisation.
 *
 * <p>Le bootstrap est désactivé en test et {@code TestUserFactory} ne crée aucune
 * {@code Organisation} : les tests provisionnent donc leurs propres données via l'API.</p>
 */
@DisplayName("GET /groups et GET /{orgId}")
class ListGroupsIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String DEFAULT_ORG = TestUserFactory.DEFAULT_ORGANISATION_ID;
    private static final String ADMIN_EMAIL = "admin.groups@alertmns.local";
    private static final String MANAGER_EMAIL = "manager.groups@alertmns.local";
    private static final String MEMBER_EMAIL = "membre.groups@alertmns.local";
    private static final String PASSWORD = "groupspassword1234";

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Autowired
    private OrganisationJpaRepository organisationJpaRepository;

    /**
     * Les organisations et groupes créés ici ne sont pas couverts par le nettoyage du parent
     * (qui ne purge que Users et Members) : sans cela, ils s'accumuleraient d'un test à l'autre
     * dans le conteneur partagé.
     */
    @AfterEach
    void cleanGroupsAndOrganisations() {
        groupJpaRepository.deleteAll();
        organisationJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("un ADMIN liste les groupes de l'organisation")
    void shouldListGroups() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);
        String orgId = createOrganisation(admin, uniqueName());
        createGroup(admin, orgId, "Promo CDA 2026");

        ResponseEntity<String> response = get("/api/organisations/" + orgId + "/groups", admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"name\":\"Promo CDA 2026\"")
                .contains("\"total\":1");
    }

    @Test
    @DisplayName("la recherche filtre sur le nom du groupe")
    void shouldFilterByName() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);
        String orgId = createOrganisation(admin, uniqueName());
        createGroup(admin, orgId, "Promo CDA 2026");
        createGroup(admin, orgId, "Enseignants");

        ResponseEntity<String> matching = get("/api/organisations/" + orgId + "/groups?q=promo", admin);
        assertThat(matching.getBody()).contains("Promo CDA 2026").doesNotContain("Enseignants");

        ResponseEntity<String> empty = get("/api/organisations/" + orgId + "/groups?q=zzz", admin);
        assertThat(empty.getBody()).contains("\"total\":0").contains("\"items\":[]");
    }

    @Test
    @DisplayName("un MANAGER peut lister les groupes : il en a besoin pour cibler une alerte")
    void shouldAllowManagerToListGroups() {
        userFactory.registerActiveManager(MANAGER_EMAIL, PASSWORD);
        AuthCookies manager = loginAndAcquireCookies(MANAGER_EMAIL, PASSWORD);

        ResponseEntity<String> response = get("/api/organisations/" + DEFAULT_ORG + "/groups", manager);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("un simple membre ne peut pas lister les groupes")
    void shouldRejectPlainMember() {
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);
        AuthCookies member = loginAndAcquireCookies(MEMBER_EMAIL, PASSWORD);

        ResponseEntity<String> response = get("/api/organisations/" + DEFAULT_ORG + "/groups", member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("un ADMIN consulte le détail de l'organisation")
    void shouldGetOrganisationDetail() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);
        String name = uniqueName();
        String orgId = createOrganisation(admin, name);

        ResponseEntity<String> response = get("/api/organisations/" + orgId, admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"organisationId\":\"" + orgId + "\"")
                .contains("\"name\":\"" + name + "\"");
    }

    @Test
    @DisplayName("une organisation inconnue renvoie 404")
    void shouldReturn404ForUnknownOrganisation() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);

        ResponseEntity<String> response = get("/api/organisations/" + UUID.randomUUID(), admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("un appel anonyme reçoit 401")
    void shouldRejectAnonymous() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/organisations/" + DEFAULT_ORG + "/groups", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- Helpers ---

    /**
     * Le nom d'organisation est unique en base et les organisations ne sont pas purgées entre les
     * tests : chaque test doit donc créer la sienne sous un nom distinct.
     */
    private static String uniqueName() {
        return "Metz Numeric School " + UUID.randomUUID();
    }

    /** @return l'identifiant de l'organisation créée, extrait de la réponse JSON */
    private String createOrganisation(AuthCookies cookies, String name) {
        ResponseEntity<String> response = mutate(
                HttpMethod.POST, "/api/organisations", "{\"name\":\"" + name + "\"}", cookies);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return extractUuid(response.getBody());
    }

    private void createGroup(AuthCookies cookies, String orgId, String name) {
        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/organisations/" + orgId + "/groups",
                "{\"name\":\"" + name + "\"}",
                cookies);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private static String extractUuid(String json) {
        var matcher = java.util.regex.Pattern
                .compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
                .matcher(json);
        assertThat(matcher.find()).as("la réponse doit contenir un UUID: %s", json).isTrue();
        return matcher.group();
    }

    private ResponseEntity<String> get(String path, AuthCookies cookies) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookies.session());
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }
}
