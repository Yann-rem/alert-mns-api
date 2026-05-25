package com.alertmns.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("FirstName")
class FirstNameTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a first name")
        void shouldCreateAFirstName() {
            String value = "John";
            FirstName firstName = FirstName.of(value);
            assertEquals("John", firstName.value());
        }

        @Test
        @DisplayName("should normalize first name by trimming spaces")
        void shouldNormalizeFirstName() {
            String value = " John ";
            FirstName firstName = FirstName.of(value);
            assertEquals("John", firstName.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null first name")
        void shouldRejectNullFirstName() {
            String value = null;
            assertThrows(NullPointerException.class, () -> FirstName.of(value));
        }

        @Test
        @DisplayName("should reject blank first name")
        void shouldRejectBlankFirstName() {
            String value = " ";
            assertThrows(IllegalArgumentException.class, () -> FirstName.of(value));
        }

        @Test
        @DisplayName("should accept first name with max length")
        void shouldAcceptFirstNameWithMaxLength() {
            String value = "a".repeat(100);
            FirstName firstName = FirstName.of(value);
            assertEquals(value, firstName.value());
        }

        @Test
        @DisplayName("should reject first name exceeding max length")
        void shouldRejectFirstNameExceedingMaxLength() {
            String value = "a".repeat(101);
            assertThrows(IllegalArgumentException.class, () -> FirstName.of(value));
        }
    }
}
