package com.alertmns.messaging.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MessageId")
class MessageIdTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should generate a unique MessageId")
        void shouldGenerateAUniqueMessageId() {
            MessageId id1 = MessageId.generate();
            MessageId id2 = MessageId.generate();
            assertNotEquals(id1, id2);
        }

        @Test
        @DisplayName("should reconstruct MessageId from a valid UUID string")
        void shouldReconstructMessageIdFromString() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            MessageId id = MessageId.from(raw);
            assertEquals(UUID.fromString(raw), id.value());
        }

        @Test
        @DisplayName("should create MessageId from a valid UUID")
        void shouldCreateMessageIdFromAValidUUID() {
            UUID uuid = UUID.randomUUID();
            MessageId id = MessageId.from(uuid);
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
            assertThrows(NullPointerException.class, () -> MessageId.from(raw));
        }

        @Test
        @DisplayName("should reject invalid UUID string")
        void shouldRejectInvalidUUID() {
            String raw = "invalid";
            assertThrows(IllegalArgumentException.class, () -> MessageId.from(raw));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two MessageIds with same UUID should be equal")
        void twoMessageIdsWithSameUUIDShouldBeEqual() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            MessageId id1 = MessageId.from(raw);
            MessageId id2 = MessageId.from(raw);
            assertEquals(id1, id2);
        }

        @Test
        @DisplayName("two MessageIds with different UUIDs should not be equal")
        void twoMessageIdsWithDifferentUUIDsShouldNotBeEqual() {
            String raw1 = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            String raw2 = "72ce2342-1d41-4e0b-b4b8-6e88891e9add";
            MessageId id1 = MessageId.from(raw1);
            MessageId id2 = MessageId.from(raw2);
            assertNotEquals(id1, id2);
        }
    }
}
