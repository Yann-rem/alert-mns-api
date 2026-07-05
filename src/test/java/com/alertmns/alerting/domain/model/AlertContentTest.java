package com.alertmns.alerting.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AlertContent")
class AlertContentTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("of should accept a valid value")
        void acceptsValidValue() {
            assertEquals("Évacuation immédiate", AlertContent.of("Évacuation immédiate").value());
        }

        @Test
        @DisplayName("of should strip surrounding whitespace")
        void stripsWhitespace() {
            assertEquals("Alerte", AlertContent.of("   Alerte   ").value());
        }

        @Test
        @DisplayName("of should accept a value at the maximum length")
        void acceptsMaxLength() {
            String maxValue = "a".repeat(4000);
            assertEquals(maxValue, AlertContent.of(maxValue).value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject a null value")
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> AlertContent.of(null));
        }

        @Test
        @DisplayName("should reject a blank value")
        void rejectsBlank() {
            assertThrows(IllegalArgumentException.class, () -> AlertContent.of("   "));
        }

        @Test
        @DisplayName("should reject a value exceeding the maximum length")
        void rejectsTooLong() {
            String tooLong = "a".repeat(4001);
            assertThrows(IllegalArgumentException.class, () -> AlertContent.of(tooLong));
        }
    }
}
