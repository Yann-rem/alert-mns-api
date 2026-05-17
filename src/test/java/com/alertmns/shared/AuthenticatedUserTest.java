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
        @DisplayName("should expose userId provided at creation")
        void shouldExposeUserId() {
            UserId userId = UserId.generate();

            AuthenticatedUser authenticated = new AuthenticatedUser(userId);

            assertEquals(userId, authenticated.userId());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null userId")
        void shouldRejectNullUserId() {
            assertThrows(NullPointerException.class,
                    () -> new AuthenticatedUser(null));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two AuthenticatedUsers with same userId should be equal")
        void twoAuthenticatedUsersWithSameUserIdShouldBeEqual() {
            UserId userId = UserId.generate();

            AuthenticatedUser a = new AuthenticatedUser(userId);
            AuthenticatedUser b = new AuthenticatedUser(userId);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("two AuthenticatedUsers with different userIds should not be equal")
        void twoAuthenticatedUsersWithDifferentUserIdsShouldNotBeEqual() {
            AuthenticatedUser a = new AuthenticatedUser(UserId.generate());
            AuthenticatedUser b = new AuthenticatedUser(UserId.generate());

            assertNotEquals(a, b);
        }
    }
}
