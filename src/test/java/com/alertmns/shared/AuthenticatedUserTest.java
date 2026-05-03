package com.alertmns.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AuthenticatedUser")
class AuthenticatedUserTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should expose userId and organisationId provided at creation")
        void shouldExposeUserIdAndOrganisationId() {
            UserId userId = UserId.generate();
            OrganisationId organisationId = OrganisationId.generate();

            AuthenticatedUser authenticated = new AuthenticatedUser(userId, organisationId);

            assertEquals(userId, authenticated.userId());
            assertEquals(organisationId, authenticated.organisationId());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null userId")
        void shouldRejectNullUserId() {
            OrganisationId organisationId = OrganisationId.generate();
            assertThrows(NullPointerException.class,
                    () -> new AuthenticatedUser(null, organisationId));
        }

        @Test
        @DisplayName("should reject null organisationId")
        void shouldRejectNullOrganisationId() {
            UserId userId = UserId.generate();
            assertThrows(NullPointerException.class,
                    () -> new AuthenticatedUser(userId, null));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two AuthenticatedUsers with same userId and organisationId should be equal")
        void twoAuthenticatedUsersWithSameValuesShouldBeEqual() {
            UserId userId = UserId.generate();
            OrganisationId organisationId = OrganisationId.generate();

            AuthenticatedUser a = new AuthenticatedUser(userId, organisationId);
            AuthenticatedUser b = new AuthenticatedUser(userId, organisationId);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("two AuthenticatedUsers with different userIds should not be equal")
        void twoAuthenticatedUsersWithDifferentUserIdsShouldNotBeEqual() {
            OrganisationId organisationId = OrganisationId.generate();
            AuthenticatedUser a = new AuthenticatedUser(UserId.generate(), organisationId);
            AuthenticatedUser b = new AuthenticatedUser(UserId.generate(), organisationId);

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("two AuthenticatedUsers with different organisationIds should not be equal")
        void twoAuthenticatedUsersWithDifferentOrganisationIdsShouldNotBeEqual() {
            UserId userId = UserId.generate();
            AuthenticatedUser a = new AuthenticatedUser(userId, OrganisationId.generate());
            AuthenticatedUser b = new AuthenticatedUser(userId, OrganisationId.generate());

            assertNotEquals(a, b);
        }
    }
}
