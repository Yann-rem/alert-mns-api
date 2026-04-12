package com.alertmns.organisation.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemberId")
class MemberIdTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should generate a unique MemberId")
        void shouldGenerateAUniqueMemberId() {
            MemberId memberId1 = MemberId.generate();
            MemberId memberId2 = MemberId.generate();
            assertNotEquals(memberId1, memberId2);
        }

        @Test
        @DisplayName("should reconstruct MemberId from a valid UUID string")
        void shouldReconstructMemberIdFromString() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            MemberId memberId = MemberId.from(raw);
            assertEquals(UUID.fromString(raw), memberId.value());
        }

        @Test
        @DisplayName("should create MemberId from a valid UUID")
        void shouldCreateMemberIdFromAValidUUID() {
            UUID uuid = UUID.randomUUID();
            MemberId memberId = MemberId.from(uuid);
            assertEquals(uuid, memberId.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null UUID")
        void shouldRejectNullUUID() {
            String raw = null;
            assertThrows(NullPointerException.class, () -> MemberId.from(raw));
        }

        @Test
        @DisplayName("should reject invalid UUID string")
        void shouldRejectInvalidUUID() {
            String raw = "invalid";
            assertThrows(IllegalArgumentException.class, () -> MemberId.from(raw));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two MemberIds with same UUID should be equal")
        void twoMemberIdsWithSameUUIDShouldBeEqual() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            MemberId memberId1 = MemberId.from(raw);
            MemberId memberId2 = MemberId.from(raw);
            assertEquals(memberId1, memberId2);
        }

        @Test
        @DisplayName("two MemberIds with different UUIDs should not be equal")
        void twoMemberIdsWithDifferentUUIDsShouldNotBeEqual() {
            String raw1 = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            String raw2 = "72ce2342-1d41-4e0b-b4b8-6e88891e9add";
            MemberId memberId1 = MemberId.from(raw1);
            MemberId memberId2 = MemberId.from(raw2);
            assertNotEquals(memberId1, memberId2);
        }
    }
}
