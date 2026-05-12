package com.alertmns.iam.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TokenHash")
class TokenHashTest {

    private static final int SHA256_HEX_LENGTH = 64;

    @Nested
    @DisplayName("Creation from RawToken")
    class CreationFromRawToken {

        @Test
        @DisplayName("should produce a 64-char hex hash")
        void shouldProduceA64CharHexHash() {
            TokenHash hash = TokenHash.of(RawToken.of("any-token"));
            assertEquals(SHA256_HEX_LENGTH, hash.hex().length());
        }

        @Test
        @DisplayName("should be deterministic for the same raw token")
        void shouldBeDeterministic() {
            TokenHash first = TokenHash.of(RawToken.of("any-token"));
            TokenHash second = TokenHash.of(RawToken.of("any-token"));
            assertEquals(first, second);
        }

        @Test
        @DisplayName("should produce different hashes for different raw tokens")
        void shouldProduceDifferentHashes() {
            TokenHash first = TokenHash.of(RawToken.of("token-a"));
            TokenHash second = TokenHash.of(RawToken.of("token-b"));
            assertNotEquals(first, second);
        }

        @Test
        @DisplayName("should match the known SHA-256 hex of a known input")
        void shouldMatchKnownSha256() {
            // SHA-256("abc") = ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
            TokenHash hash = TokenHash.of(RawToken.of("abc"));
            assertEquals(
                    "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                    hash.hex());
        }

        @Test
        @DisplayName("should reject null raw token")
        void shouldRejectNullRawToken() {
            assertThrows(NullPointerException.class, () -> TokenHash.of(null));
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null hex")
        void shouldRejectNullHex() {
            String hex = null;
            assertThrows(NullPointerException.class, () -> new TokenHash(hex));
        }

        @Test
        @DisplayName("should reject hex shorter than 64 characters")
        void shouldRejectHexShorterThan64() {
            String hex = "a".repeat(SHA256_HEX_LENGTH - 1);
            assertThrows(IllegalArgumentException.class, () -> new TokenHash(hex));
        }

        @Test
        @DisplayName("should reject hex longer than 64 characters")
        void shouldRejectHexLongerThan64() {
            String hex = "a".repeat(SHA256_HEX_LENGTH + 1);
            assertThrows(IllegalArgumentException.class, () -> new TokenHash(hex));
        }

        @Test
        @DisplayName("should accept hex of exactly 64 characters")
        void shouldAcceptHexOf64Chars() {
            String hex = "a".repeat(SHA256_HEX_LENGTH);
            TokenHash tokenHash = new TokenHash(hex);
            assertEquals(hex, tokenHash.hex());
        }
    }
}
