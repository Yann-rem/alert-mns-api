package com.alertmns.iam.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ActivationTokenId")
class ActivationTokenIdTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should generate a unique ActivationTokenId")
        void shouldGenerateAUniqueActivationTokenId() {
            ActivationTokenId id1 = ActivationTokenId.generate();
            ActivationTokenId id2 = ActivationTokenId.generate();
            assertNotEquals(id1, id2);
        }

        @Test
        @DisplayName("should create ActivationTokenId from a valid UUID")
        void shouldCreateActivationTokenIdFromAValidUUID() {
            UUID uuid = UUID.randomUUID();
            ActivationTokenId id = ActivationTokenId.from(uuid);
            assertEquals(uuid, id.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null UUID")
        void shouldRejectNullUUID() {
            UUID uuid = null;
            assertThrows(NullPointerException.class, () -> ActivationTokenId.from(uuid));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two ActivationTokenIds with same UUID should be equal")
        void twoActivationTokenIdsWithSameUUIDShouldBeEqual() {
            UUID uuid = UUID.randomUUID();
            ActivationTokenId id1 = ActivationTokenId.from(uuid);
            ActivationTokenId id2 = ActivationTokenId.from(uuid);
            assertEquals(id1, id2);
        }

        @Test
        @DisplayName("two ActivationTokenIds with different UUIDs should not be equal")
        void twoActivationTokenIdsWithDifferentUUIDsShouldNotBeEqual() {
            ActivationTokenId id1 = ActivationTokenId.from(UUID.randomUUID());
            ActivationTokenId id2 = ActivationTokenId.from(UUID.randomUUID());
            assertNotEquals(id1, id2);
        }
    }
}
