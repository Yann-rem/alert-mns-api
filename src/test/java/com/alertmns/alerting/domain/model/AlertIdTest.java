package com.alertmns.alerting.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AlertId")
class AlertIdTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("generate should produce a non-null random identifier")
        void generateProducesNonNull() {
            assertNotNull(AlertId.generate().value());
        }

        @Test
        @DisplayName("generate should produce distinct identifiers")
        void generateProducesDistinct() {
            assertNotEquals(AlertId.generate(), AlertId.generate());
        }

        @Test
        @DisplayName("from(UUID) should wrap the given value")
        void fromUuidWraps() {
            UUID uuid = UUID.randomUUID();
            assertEquals(uuid, AlertId.from(uuid).value());
        }

        @Test
        @DisplayName("from(String) should parse a valid UUID string")
        void fromStringParses() {
            UUID uuid = UUID.randomUUID();
            assertEquals(uuid, AlertId.from(uuid.toString()).value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject a null value")
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> new AlertId(null));
        }

        @Test
        @DisplayName("from(String) should reject an invalid UUID format")
        void fromStringRejectsInvalid() {
            assertThrows(IllegalArgumentException.class, () -> AlertId.from("not-a-uuid"));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for the same value")
        void equalForSameValue() {
            UUID uuid = UUID.randomUUID();
            assertEquals(AlertId.from(uuid), AlertId.from(uuid));
        }

        @Test
        @DisplayName("should not be equal for different values")
        void notEqualForDifferentValues() {
            assertNotEquals(AlertId.from(UUID.randomUUID()), AlertId.from(UUID.randomUUID()));
        }
    }
}
