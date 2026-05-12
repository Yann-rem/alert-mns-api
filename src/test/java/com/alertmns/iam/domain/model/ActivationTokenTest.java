package com.alertmns.iam.domain.model;

import com.alertmns.iam.domain.exception.ActivationTokenExpiredException;
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

    @Nested
    @DisplayName("Issuance")
    class Issuance {

        @Test
        @DisplayName("should issue a token with the hashed raw token")
        void shouldIssueATokenWithHashedRawToken() {
            ActivationToken.IssuedToken issued = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL);

            assertEquals(TokenHash.of(RAW_TOKEN), issued.activationToken().tokenHash());
        }

        @Test
        @DisplayName("should expose the raw token in the issued result")
        void shouldExposeRawTokenInIssuedResult() {
            ActivationToken.IssuedToken issued = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL);

            assertSame(RAW_TOKEN, issued.rawToken());
        }

        @Test
        @DisplayName("should assign the target user id")
        void shouldAssignTargetUserId() {
            ActivationToken.IssuedToken issued = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL);

            assertEquals(USER_ID, issued.activationToken().userId());
        }

        @Test
        @DisplayName("should generate a fresh id")
        void shouldGenerateFreshId() {
            ActivationToken first = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL).activationToken();
            ActivationToken second = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL).activationToken();

            assertNotNull(first.id());
            assertNotNull(second.id());
            assertFalse(first.id().equals(second.id()));
        }

        @Test
        @DisplayName("should set expiresAt to createdAt + ttl")
        void shouldSetExpiresAtToCreatedAtPlusTtl() {
            ActivationToken token = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL).activationToken();

            assertEquals(token.createdAt().plus(TTL), token.expiresAt());
        }
    }

    @Nested
    @DisplayName("Expiration")
    class Expiration {

        @Test
        @DisplayName("should not be expired right after issuance")
        void shouldNotBeExpiredJustAfterIssuance() {
            ActivationToken token = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL).activationToken();

            assertFalse(token.isExpired());
        }

        @Test
        @DisplayName("should be expired when expiresAt is in the past")
        void shouldBeExpiredWhenExpiresAtInPast() {
            ActivationToken token = ActivationToken.reconstitute(
                    ActivationTokenId.generate(),
                    USER_ID,
                    TokenHash.of(RAW_TOKEN),
                    Instant.now().minus(Duration.ofHours(49)),
                    Instant.now().minus(Duration.ofHours(1))
            );

            assertTrue(token.isExpired());
        }

        @Test
        @DisplayName("verifyUsable should throw when expired")
        void verifyUsableShouldThrowWhenExpired() {
            ActivationToken token = ActivationToken.reconstitute(
                    ActivationTokenId.generate(),
                    USER_ID,
                    TokenHash.of(RAW_TOKEN),
                    Instant.now().minus(Duration.ofHours(49)),
                    Instant.now().minus(Duration.ofHours(1))
            );

            assertThrows(ActivationTokenExpiredException.class, token::verifyUsable);
        }

        @Test
        @DisplayName("verifyUsable should not throw when still valid")
        void verifyUsableShouldNotThrowWhenStillValid() {
            ActivationToken token = ActivationToken.issue(USER_ID, RAW_TOKEN, TTL).activationToken();

            token.verifyUsable();
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null userId on issue")
        void shouldRejectNullUserIdOnIssue() {
            assertThrows(NullPointerException.class,
                    () -> ActivationToken.issue(null, RAW_TOKEN, TTL));
        }

        @Test
        @DisplayName("should reject null rawToken on issue")
        void shouldRejectNullRawTokenOnIssue() {
            assertThrows(NullPointerException.class,
                    () -> ActivationToken.issue(USER_ID, null, TTL));
        }

        @Test
        @DisplayName("should reject null ttl on issue")
        void shouldRejectNullTtlOnIssue() {
            assertThrows(NullPointerException.class,
                    () -> ActivationToken.issue(USER_ID, RAW_TOKEN, null));
        }
    }
}
