package com.alertmns.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RawPassword")
class RawPasswordTest {

    private static final int MIN_LENGTH = 12;

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a raw password")
        void shouldCreateARawPassword() {
            String value = "a".repeat(MIN_LENGTH);
            RawPassword rawPassword = RawPassword.of(value);
            assertEquals(value, rawPassword.value());
        }

        @Test
        @DisplayName("should accept a value longer than the minimum")
        void shouldAcceptValueLongerThanMin() {
            String value = "a".repeat(MIN_LENGTH + 50);
            RawPassword rawPassword = RawPassword.of(value);
            assertEquals(value, rawPassword.value());
        }

        @Test
        @DisplayName("should not normalize the value (no strip, no lowercase)")
        void shouldNotNormalizeTheValue() {
            String value = " HelloPwd123 ";
            RawPassword rawPassword = RawPassword.of(value);
            assertEquals(" HelloPwd123 ", rawPassword.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null value")
        void shouldRejectNullValue() {
            String value = null;
            assertThrows(NullPointerException.class, () -> RawPassword.of(value));
        }

        @Test
        @DisplayName("should reject value shorter than the minimum length")
        void shouldRejectValueShorterThanMin() {
            String value = "a".repeat(MIN_LENGTH - 1);
            assertThrows(IllegalArgumentException.class, () -> RawPassword.of(value));
        }

        @Test
        @DisplayName("should accept value with exactly the minimum length")
        void shouldAcceptValueWithExactlyMinLength() {
            String value = "a".repeat(MIN_LENGTH);
            RawPassword rawPassword = RawPassword.of(value);
            assertEquals(MIN_LENGTH, rawPassword.value().length());
        }
    }

    @Nested
    @DisplayName("Security")
    class Security {

        @Test
        @DisplayName("toString should never expose the value in clear")
        void toStringShouldNeverExposeValue() {
            String secret = "supersecret123";
            RawPassword rawPassword = RawPassword.of(secret);
            assertFalse(rawPassword.toString().contains(secret),
                    "toString should mask the raw password, but was: " + rawPassword);
        }
    }
}
