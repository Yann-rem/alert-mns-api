package com.alertmns.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HashedPassword")
class HashedPasswordTest {

    static final String BCRYPT_HASH =
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

    @Nested
    @DisplayName("Unset sentinel")
    class UnsetSentinel {

        @Test
        @DisplayName("unset() should return a non-null instance satisfying the invariants")
        void unsetShouldReturnANonNullInstance() {
            HashedPassword unset = HashedPassword.unset();
            // L'invariant non-blank / max length doit être respecté par la sentinelle.
            assertEquals("$unset$", unset.value());
        }

        @Test
        @DisplayName("unset() should not look like a bcrypt hash")
        void unsetShouldNotLookLikeABcryptHash() {
            HashedPassword unset = HashedPassword.unset();
            // Garantit que la sentinelle ne pourra jamais être matchée par BCryptPasswordEncoder.matches(),
            // qui rejette tout encodedPassword ne correspondant pas au pattern bcrypt.
            assertFalse(unset.value().startsWith("$2a$"));
            assertFalse(unset.value().startsWith("$2b$"));
            assertFalse(unset.value().startsWith("$2y$"));
        }

        @Test
        @DisplayName("isUnset() should be true for the sentinel")
        void isUnsetShouldBeTrueForTheSentinel() {
            assertTrue(HashedPassword.unset().isUnset());
        }

        @Test
        @DisplayName("isUnset() should be false for a real hash")
        void isUnsetShouldBeFalseForARealHash() {
            assertFalse(HashedPassword.of(BCRYPT_HASH).isUnset());
        }

        @Test
        @DisplayName("two unset() instances should be equal")
        void twoUnsetInstancesShouldBeEqual() {
            assertEquals(HashedPassword.unset(), HashedPassword.unset());
        }

        @Test
        @DisplayName("unset() should not be equal to a real hash")
        void unsetShouldNotBeEqualToARealHash() {
            assertNotEquals(HashedPassword.unset(), HashedPassword.of(BCRYPT_HASH));
        }
    }
}
