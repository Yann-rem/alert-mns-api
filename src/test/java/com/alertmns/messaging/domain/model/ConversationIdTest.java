package com.alertmns.messaging.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ConversationId")
class ConversationIdTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should generate a unique ConversationId")
        void shouldGenerateAUniqueConversationId() {
            ConversationId id1 = ConversationId.generate();
            ConversationId id2 = ConversationId.generate();
            assertNotEquals(id1, id2);
        }

        @Test
        @DisplayName("should reconstruct ConversationId from a valid UUID string")
        void shouldReconstructConversationIdFromString() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            ConversationId id = ConversationId.from(raw);
            assertEquals(UUID.fromString(raw), id.value());
        }

        @Test
        @DisplayName("should create ConversationId from a valid UUID")
        void shouldCreateConversationIdFromAValidUUID() {
            UUID uuid = UUID.randomUUID();
            ConversationId id = ConversationId.from(uuid);
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
            assertThrows(NullPointerException.class, () -> ConversationId.from(raw));
        }

        @Test
        @DisplayName("should reject invalid UUID string")
        void shouldRejectInvalidUUID() {
            String raw = "invalid";
            assertThrows(IllegalArgumentException.class, () -> ConversationId.from(raw));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two ConversationIds with same UUID should be equal")
        void twoConversationIdsWithSameUUIDShouldBeEqual() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            ConversationId id1 = ConversationId.from(raw);
            ConversationId id2 = ConversationId.from(raw);
            assertEquals(id1, id2);
        }

        @Test
        @DisplayName("two ConversationIds with different UUIDs should not be equal")
        void twoConversationIdsWithDifferentUUIDsShouldNotBeEqual() {
            String raw1 = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            String raw2 = "72ce2342-1d41-4e0b-b4b8-6e88891e9add";
            ConversationId id1 = ConversationId.from(raw1);
            ConversationId id2 = ConversationId.from(raw2);
            assertNotEquals(id1, id2);
        }
    }
}
