package com.alertmns.iam.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HashedPassword")
class HashedPasswordTest {

    private static final String BCRYPT_HASH =
            "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a hashed password")
        void shouldCreateAHashedPassword() {
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            assertEquals(BCRYPT_HASH, hashedPassword.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null hashed password")
        void shouldRejectNullHashedPassword() {
            assertThrows(NullPointerException.class, () -> HashedPassword.of(null));
        }

        @Test
        @DisplayName("should reject blank hashed password")
        void shouldRejectBlankHashedPassword() {
            assertThrows(IllegalArgumentException.class, () -> HashedPassword.of(" "));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two identical hashed passwords should be equal")
        void twoIdenticalHashedPasswordsShouldBeEqual() {
            assertEquals(HashedPassword.of(BCRYPT_HASH), HashedPassword.of(BCRYPT_HASH));
        }

        @Test
        @DisplayName("two different hashed passwords should not be equal")
        void twoDifferentHashedPasswordsShouldNotBeEqual() {
            String otherHash = "$2a$10$zyxwvutsrqponmlkjihgffeZYXWVUTSRQPONMLKJIHGFEDCBA987654";
            assertNotEquals(HashedPassword.of(BCRYPT_HASH), HashedPassword.of(otherHash));
        }
    }
}
