package com.alertmns.iam.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RawToken")
class RawTokenTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a raw token")
        void shouldCreateARawToken() {
            String value = "abc123";
            RawToken rawToken = RawToken.of(value);
            assertEquals("abc123", rawToken.value());
        }

        @Test
        @DisplayName("should not normalize the value to keep the hash deterministic")
        void shouldNotNormalizeTheValue() {
            String value = " abc123 ";
            RawToken rawToken = RawToken.of(value);
            assertEquals(" abc123 ", rawToken.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null value")
        void shouldRejectNullValue() {
            String value = null;
            assertThrows(NullPointerException.class, () -> RawToken.of(value));
        }

        @Test
        @DisplayName("should reject blank value")
        void shouldRejectBlankValue() {
            String value = " ";
            assertThrows(IllegalArgumentException.class, () -> RawToken.of(value));
        }

        @Test
        @DisplayName("should reject empty value")
        void shouldRejectEmptyValue() {
            String value = "";
            assertThrows(IllegalArgumentException.class, () -> RawToken.of(value));
        }
    }
}
