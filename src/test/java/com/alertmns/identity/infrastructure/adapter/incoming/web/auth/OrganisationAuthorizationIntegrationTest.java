package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration end-to-end couvrant les annotations {@code @PreAuthorize("hasRole('ADMIN')")} des controllers
 * d'administration du BC Organisation ({@code OrganisationController}, {@code GroupController},
 * {@code MemberController}, {@code GroupMembershipController}).
 *
 * <p>Contrat vérifié :</p>
 * <ul>
 *   <li>un membre non-admin (ROLE_MEMBER) est refusé (403) sur chaque controller admin ;</li>
 *   <li>un administrateur (ROLE_ADMIN) franchit la barrière : 201 sur un happy path, 404 quand la cible n'existe
 *       pas — mais jamais 403.</li>
 * </ul>
 *
 * <p>On passe par un vrai login (session + CSRF) plutôt que {@code @WithMockUser}, car les autorités sont dérivées du
 * {@code Member.role} réel via {@code MemberUserAuthoritiesAdapter}.</p>
 *
 * <p>Le test est co-localisé dans ce package pour hériter du harnais {@link AbstractAuthIntegrationTest}
 * (package-private), à l'image de {@code UserAuthorizationIntegrationTest}.</p>
 */
@DisplayName("@PreAuthorize on /api/organisations/**")
class OrganisationAuthorizationIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.org.authz@alertmns.local";
    private static final String MEMBER_EMAIL = "member.org.authz@alertmns.local";
    private static final String PASSWORD = "secret123456";

    private static final String ORG_ID = "00000000-0000-0000-0000-000000000001";

    private AuthCookies asMember() {
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);
        return loginAndAcquireCookies(MEMBER_EMAIL, PASSWORD);
    }

    private AuthCookies asAdmin() {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        return loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // OrganisationController : POST /api/organisations
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    @DisplayName("Member creating an organisation returns 403")
    void memberCreateOrganisationForbidden() {
        AuthCookies member = asMember();

        ResponseEntity<String> response = mutate(
                HttpMethod.POST, "/api/organisations", "{\"name\":\"Some Org\"}", member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Admin creating an organisation returns 201")
    void adminCreateOrganisationCreated() {
        AuthCookies admin = asAdmin();

        ResponseEntity<String> response = mutate(
                HttpMethod.POST, "/api/organisations", "{\"name\":\"Brand New Org\"}", admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // GroupController : POST /api/organisations/{orgId}/groups
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    @DisplayName("Member creating a group returns 403")
    void memberCreateGroupForbidden() {
        AuthCookies member = asMember();

        ResponseEntity<String> response = mutate(
                HttpMethod.POST, "/api/organisations/" + ORG_ID + "/groups", "{\"name\":\"CDA\"}", member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // MemberController : POST .../members/{id}/suspend & .../reactivate
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    @DisplayName("Member suspending a member returns 403")
    void memberSuspendForbidden() {
        AuthCookies member = asMember();

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/organisations/" + ORG_ID + "/members/" + UUID.randomUUID() + "/suspend",
                null, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Admin suspending an unknown member returns 404 (past the gate, not 403)")
    void adminSuspendUnknownMemberNotFound() {
        AuthCookies admin = asAdmin();

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/organisations/" + ORG_ID + "/members/" + UUID.randomUUID() + "/suspend",
                null, admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Member reactivating a member returns 403")
    void memberReactivateForbidden() {
        AuthCookies member = asMember();

        ResponseEntity<String> response = mutate(
                HttpMethod.POST,
                "/api/organisations/" + ORG_ID + "/members/" + UUID.randomUUID() + "/reactivate",
                null, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Member changing a member's role returns 403")
    void memberChangeRoleForbidden() {
        AuthCookies member = asMember();

        ResponseEntity<String> response = mutate(
                HttpMethod.PUT,
                "/api/organisations/" + ORG_ID + "/members/" + UUID.randomUUID() + "/role",
                "{\"role\":\"MANAGER\"}", member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // GroupMembershipController : PUT/DELETE .../groups/{groupId}/members/{memberId}
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    @DisplayName("Member adding a member to a group returns 403")
    void memberAddMembershipForbidden() {
        AuthCookies member = asMember();

        ResponseEntity<String> response = mutate(
                HttpMethod.PUT,
                "/api/organisations/" + ORG_ID + "/groups/" + UUID.randomUUID() + "/members/" + UUID.randomUUID(),
                null, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Member removing a member from a group returns 403")
    void memberRemoveMembershipForbidden() {
        AuthCookies member = asMember();

        ResponseEntity<String> response = mutate(
                HttpMethod.DELETE,
                "/api/organisations/" + ORG_ID + "/groups/" + UUID.randomUUID() + "/members/" + UUID.randomUUID(),
                null, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
