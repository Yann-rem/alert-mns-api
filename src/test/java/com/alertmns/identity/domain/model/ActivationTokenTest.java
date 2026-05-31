package com.alertmns.identity.domain.model;

import com.alertmns.identity.domain.exception.ActivationTokenExpiredException;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ActivationToken")
class ActivationTokenTest {

    static final UserId USER_ID = UserId.generate();
    static final RawToken RAW_TOKEN = RawToken.of("any-raw-token");
    static final Duration TTL = Duration.ofHours(48);
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Nested
    @DisplayName("Issuance")
    class Issuance {

        @Test
        @DisplayName("should issue a token with the hashed raw token")
        void shouldIssueATokenWithHashedRawToken() {
            ActivationToken.IssuedToken issued = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW);

            assertEquals(HashedToken.of(RAW_TOKEN), issued.activationToken().hash());
        }

        @Test
        @DisplayName("should expose the raw token in the issued result")
        void shouldExposeRawTokenInIssuedResult() {
            ActivationToken.IssuedToken issued = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW);

            assertSame(RAW_TOKEN, issued.rawToken());
        }

        @Test
        @DisplayName("should assign the target user id")
        void shouldAssignTargetUserId() {
            ActivationToken.IssuedToken issued = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW);

            assertEquals(USER_ID, issued.activationToken().userId());
        }

        @Test
        @DisplayName("should generate a fresh id")
        void shouldGenerateFreshId() {
            ActivationToken first = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW).activationToken();
            ActivationToken second = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW).activationToken();

            assertNotNull(first.id());
            assertNotNull(second.id());
            assertFalse(first.id().equals(second.id()));
        }

        @Test
        @DisplayName("should set createdAt to now and expiresAt to now + ttl")
        void shouldSetTimestampsFromNow() {
            ActivationToken token = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW).activationToken();

            assertEquals(NOW, token.createdAt());
            assertEquals(NOW.plus(TTL), token.expiresAt());
        }
    }

    @Nested
    @DisplayName("Expiration")
    class Expiration {

        @Test
        @DisplayName("should not be expired right after issuance")
        void shouldNotBeExpiredJustAfterIssuance() {
            ActivationToken token = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW).activationToken();

            assertFalse(token.isExpired(NOW));
        }

        @Test
        @DisplayName("should not be expired exactly at expiresAt (boundary inclusive)")
        void shouldNotBeExpiredExactlyAtExpiresAt() {
            ActivationToken token = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW).activationToken();

            assertFalse(token.isExpired(token.expiresAt()));
        }

        @Test
        @DisplayName("should be expired one second after expiresAt")
        void shouldBeExpiredOneSecondAfterExpiresAt() {
            ActivationToken token = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW).activationToken();

            assertTrue(token.isExpired(token.expiresAt().plusSeconds(1)));
        }

        @Test
        @DisplayName("verifyUsable should throw when now > expiresAt")
        void verifyUsableShouldThrowWhenExpired() {
            ActivationToken token = ActivationToken.reconstitute(
                    ActivationTokenId.generate(),
                    USER_ID,
                    HashedToken.of(RAW_TOKEN),
                    NOW.minus(Duration.ofHours(49)),
                    NOW.minus(Duration.ofHours(1))
            );

            assertThrows(ActivationTokenExpiredException.class, () -> token.verifyUsable(NOW));
        }

        @Test
        @DisplayName("verifyUsable should not throw when still valid")
        void verifyUsableShouldNotThrowWhenStillValid() {
            ActivationToken token = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL, NOW).activationToken();

            token.verifyUsable(NOW);
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null userId on issue")
        void shouldRejectNullUserIdOnIssue() {
            assertThrows(NullPointerException.class,
                    () -> ActivationToken.issue(null, RAW_TOKEN, TTL, NOW));
        }

        @Test
        @DisplayName("should reject null rawToken on issue")
        void shouldRejectNullRawTokenOnIssue() {
            assertThrows(NullPointerException.class,
                    () -> ActivationToken.issue(USER_ID, null, TTL, NOW));
        }

        @Test
        @DisplayName("should reject null ttl on issue")
        void shouldRejectNullTtlOnIssue() {
            assertThrows(NullPointerException.class,
                    () -> ActivationToken.issue(USER_ID, RAW_TOKEN, null, NOW));
        }
    }
}
