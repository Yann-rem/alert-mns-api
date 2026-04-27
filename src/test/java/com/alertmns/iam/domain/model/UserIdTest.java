package com.alertmns.iam.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UserId")
class UserIdTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should generate a unique UserId")
        void shouldGenerateAUniqueUserId() {
            UserId userId1 = UserId.generate();
            UserId userId2 = UserId.generate();
            assertNotEquals(userId1, userId2);
        }

        @Test
        @DisplayName("should reconstruct UserId from a valid UUID string")
        void shouldReconstructUserIdFromString() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            UserId userId = UserId.from(raw);
            assertEquals(UUID.fromString(raw), userId.value());
        }

        @Test
        @DisplayName("should create UserId from a valid UUID")
        void shouldCreateUserIdFromAValidUUID() {
            UUID uuid = UUID.randomUUID();
            UserId userId = UserId.from(uuid);
            assertEquals(uuid, userId.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null UUID")
        void shouldRejectNullUUID() {
            String raw = null;
            assertThrows(NullPointerException.class, () -> UserId.from(raw));
        }

        @Test
        @DisplayName("should reject invalid UUID string")
        void shouldRejectInvalidUUID() {
            String raw = "invalid";
            assertThrows(IllegalArgumentException.class, () -> UserId.from(raw));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two UserIds with same UUID should be equal")
        void twoUserIdsWithSameUUIDShouldBeEqual() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            UserId userId1 = UserId.from(raw);
            UserId userId2 = UserId.from(raw);
            assertEquals(userId1, userId2);
        }

        @Test
        @DisplayName("two UserIds with different UUIDs should not be equal")
        void twoUserIdsWithDifferentUUIDsShouldNotBeEqual() {
            String raw1 = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            String raw2 = "72ce2342-1d41-4e0b-b4b8-6e88891e9add";
            UserId userId1 = UserId.from(raw1);
            UserId userId2 = UserId.from(raw2);
            assertNotEquals(userId1, userId2);
        }
    }
}
