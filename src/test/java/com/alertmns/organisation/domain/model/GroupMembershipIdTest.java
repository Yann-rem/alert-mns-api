package com.alertmns.organisation.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GroupMembershipId")
class GroupMembershipIdTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should generate a unique GroupMembershipId")
        void shouldGenerateAUniqueGroupMembershipId() {
            GroupMembershipId id1 = GroupMembershipId.generate();
            GroupMembershipId id2 = GroupMembershipId.generate();
            assertNotEquals(id1, id2);
        }

        @Test
        @DisplayName("should reconstruct GroupMembershipId from a valid UUID string")
        void shouldReconstructGroupMembershipIdFromString() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            GroupMembershipId id = GroupMembershipId.from(raw);
            assertEquals(UUID.fromString(raw), id.value());
        }

        @Test
        @DisplayName("should create GroupMembershipId from a valid UUID")
        void shouldCreateGroupMembershipIdFromAValidUUID() {
            UUID uuid = UUID.randomUUID();
            GroupMembershipId id = GroupMembershipId.from(uuid);
            assertEquals(uuid, id.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null UUID")
        void shouldRejectNullUUID() {
            String raw = null;
            assertThrows(NullPointerException.class, () -> GroupMembershipId.from(raw));
        }

        @Test
        @DisplayName("should reject invalid UUID string")
        void shouldRejectInvalidUUID() {
            String raw = "invalid";
            assertThrows(IllegalArgumentException.class, () -> GroupMembershipId.from(raw));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two GroupMembershipIds with same UUID should be equal")
        void twoGroupMembershipIdsWithSameUUIDShouldBeEqual() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            GroupMembershipId id1 = GroupMembershipId.from(raw);
            GroupMembershipId id2 = GroupMembershipId.from(raw);
            assertEquals(id1, id2);
        }

        @Test
        @DisplayName("two GroupMembershipIds with different UUIDs should not be equal")
        void twoGroupMembershipIdsWithDifferentUUIDsShouldNotBeEqual() {
            String raw1 = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            String raw2 = "72ce2342-1d41-4e0b-b4b8-6e88891e9add";
            GroupMembershipId id1 = GroupMembershipId.from(raw1);
            GroupMembershipId id2 = GroupMembershipId.from(raw2);
            assertNotEquals(id1, id2);
        }
    }
}
