package com.alertmns.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Email")
class EmailTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a valid email")
        void shouldCreateAValidEmail() {
            String raw = "johndoe@example.com";
            Email email = Email.of(raw);
            assertEquals("johndoe@example.com", email.value());
        }

        @Test
        @DisplayName("should normalize email to lowercase and trim spaces")
        void shouldNormalizeEmail() {
            String raw = " JOHNDOE@EXAMPLE.COM ";
            Email email = Email.of(raw);
            assertEquals("johndoe@example.com", email.value());
        }

        @Test
        @DisplayName("should accept + character in local part")
        void shouldAcceptPlusSign() {
            String raw = "john+doe@example.com";
            Email email = Email.of(raw);
            assertEquals("john+doe@example.com", email.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null email")
        void shouldRejectNullEmail() {
            String raw = null;
            assertThrows(NullPointerException.class, () -> Email.of(raw));
        }

        @Test
        @DisplayName("should reject email without @")
        void shouldRejectEmailWithoutAt() {
            String raw = "invalid";
            assertThrows(IllegalArgumentException.class, () -> Email.of(raw));
        }

        @Test
        @DisplayName("should reject email without local part")
        void shouldRejectEmailWithoutLocalPart() {
            String raw = "@example.com";
            assertThrows(IllegalArgumentException.class, () -> Email.of(raw));
        }

        @Test
        @DisplayName("should reject email without domain")
        void shouldRejectEmailWithoutDomain() {
            String raw = "johndoe@";
            assertThrows(IllegalArgumentException.class, () -> Email.of(raw));
        }

        @Test
        @DisplayName("should reject email with too short top level domain")
        void shouldRejectEmailWithTooShortTopLevelDomain() {
            String raw = "johndoe@example.c";
            assertThrows(IllegalArgumentException.class, () -> Email.of(raw));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two identical emails should be equal")
        void twoIdenticalEmailsShouldBeEqual() {
            Email email1 = Email.of("johndoe@example.com");
            Email email2 = Email.of("johndoe@example.com");
            assertEquals(email1, email2);
        }

        @Test
        @DisplayName("equality should be case insensitive")
        void equalityShouldBeCaseInsensitive() {
            Email email1 = Email.of("johndoe@example.com");
            Email email2 = Email.of("JOHNDOE@EXAMPLE.COM");
            assertEquals(email1, email2);
        }

        @Test
        @DisplayName("two different emails should not be equal")
        void twoDifferentEmailsShouldNotBeEqual() {
            Email email1 = Email.of("johndoe@example.com");
            Email email2 = Email.of("janedoe@example.com");
            assertNotEquals(email1, email2);
        }

        @Test
        @DisplayName("equal emails should have the same hash code")
        void equalEmailsShouldHaveTheSameHashCode() {
            Email email1 = Email.of("johndoe@example.com");
            Email email2 = Email.of("johndoe@example.com");
            assertEquals(email1.hashCode(), email2.hashCode());
        }
    }
}
