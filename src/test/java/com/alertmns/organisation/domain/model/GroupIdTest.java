package com.alertmns.organisation.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GroupId")
class GroupIdTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should generate a unique GroupId")
        void shouldGenerateAUniqueGroupId() {
            GroupId groupId1 = GroupId.generate();
            GroupId groupId2 = GroupId.generate();
            assertNotEquals(groupId1, groupId2);
        }

        @Test
        @DisplayName("should reconstruct GroupId from a valid UUID string")
        void shouldReconstructGroupIdFromString() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            GroupId groupId = GroupId.from(raw);
            assertEquals(UUID.fromString(raw), groupId.value());
        }

        @Test
        @DisplayName("should create GroupId from a valid UUID")
        void shouldCreateGroupIdFromAValidUUID() {
            UUID uuid = UUID.randomUUID();
            GroupId groupId = GroupId.from(uuid);
            assertEquals(uuid, groupId.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null UUID")
        void shouldRejectNullUUID() {
            String raw = null;
            assertThrows(NullPointerException.class, () -> GroupId.from(raw));
        }

        @Test
        @DisplayName("should reject invalid UUID string")
        void shouldRejectInvalidUUID() {
            String raw = "invalid";
            assertThrows(IllegalArgumentException.class, () -> GroupId.from(raw));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two GroupIds with same UUID should be equal")
        void twoGroupIdsWithSameUUIDShouldBeEqual() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            GroupId groupId1 = GroupId.from(raw);
            GroupId groupId2 = GroupId.from(raw);
            assertEquals(groupId1, groupId2);
        }

        @Test
        @DisplayName("two GroupIds with different UUIDs should not be equal")
        void twoGroupIdsWithDifferentUUIDsShouldNotBeEqual() {
            String raw1 = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            String raw2 = "72ce2342-1d41-4e0b-b4b8-6e88891e9add";
            GroupId groupId1 = GroupId.from(raw1);
            GroupId groupId2 = GroupId.from(raw2);
            assertNotEquals(groupId1, groupId2);
        }
    }
}
