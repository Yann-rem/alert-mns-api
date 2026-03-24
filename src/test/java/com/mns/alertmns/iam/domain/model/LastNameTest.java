package com.mns.alertmns.iam.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("LastName")
class LastNameTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a last name")
        void shouldCreateALastName() {
            String value = "Doe";
            LastName lastName = LastName.of(value);
            assertEquals("Doe", lastName.value());
        }

        @Test
        @DisplayName("should normalize last name by trimming spaces")
        void shouldNormalizeLastName() {
            String value = " Doe ";
            LastName lastName = LastName.of(value);
            assertEquals("Doe", lastName.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null last name")
        void shouldRejectNullLastName() {
            String value = null;
            assertThrows(NullPointerException.class, () -> LastName.of(value));
        }

        @Test
        @DisplayName("should reject blank last name")
        void shouldRejectBlankLastName() {
            String value = " ";
            assertThrows(IllegalArgumentException.class, () -> LastName.of(value));
        }

        @Test
        @DisplayName("should accept last name with max length")
        void shouldAcceptLastNameWithMaxLength() {
            String value = "a".repeat(100);
            LastName lastName = LastName.of(value);
            assertEquals(value, lastName.value());
        }

        @Test
        @DisplayName("should reject last name exceeding max length")
        void shouldRejectLastNameExceedingMaxLength() {
            String value = "a".repeat(101);
            assertThrows(IllegalArgumentException.class, () -> LastName.of(value));
        }
    }
}
