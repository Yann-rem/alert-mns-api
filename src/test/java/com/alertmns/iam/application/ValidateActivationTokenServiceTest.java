package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.ActivationTokenExpiredException;
import com.alertmns.iam.domain.exception.ActivationTokenNotFoundException;
import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.ActivationToken;
import com.alertmns.iam.domain.model.ActivationTokenId;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.RawToken;
import com.alertmns.iam.domain.model.TokenHash;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.incoming.query.ValidateActivationTokenQuery;
import com.alertmns.iam.domain.port.incoming.result.ActivationTokenContext;
import com.alertmns.iam.domain.port.outgoing.ActivationTokenRepository;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("ValidateActivationTokenService")
@ExtendWith(MockitoExtension.class)
class ValidateActivationTokenServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final String EMAIL = "johndoe@example.com";
    static final String FIRST_NAME = "John";
    static final String LAST_NAME = "Doe";
    static final String BCRYPT_HASH = "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";
    static final String RAW_TOKEN = "any-raw-token-value";
    static final Duration TTL = Duration.ofHours(48);

    @Mock
    ActivationTokenRepository tokenRepository;

    @Mock
    UserRepository userRepository;

    ValidateActivationTokenService service;
    User user;
    ActivationToken validToken;

    @BeforeEach
    void setUp() {
        service = new ValidateActivationTokenService(tokenRepository, userRepository);
        user = User.register(
                ORGANISATION_ID,
                Email.of(EMAIL),
                HashedPassword.of(BCRYPT_HASH),
                Profile.of(FirstName.of(FIRST_NAME), LastName.of(LAST_NAME))
        );
        validToken = ActivationToken.issue(user.id(), RawToken.of(RAW_TOKEN), TTL).activationToken();
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should return context with user's email, firstName and lastName")
        void shouldReturnContextWithUserIdentity() {
            TokenHash hash = TokenHash.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

            ActivationTokenContext context = service.validate(new ValidateActivationTokenQuery(RAW_TOKEN));

            assertEquals(Email.of(EMAIL), context.email());
            assertEquals(FirstName.of(FIRST_NAME), context.firstName());
            assertEquals(LastName.of(LAST_NAME), context.lastName());
        }

        @Test
        @DisplayName("should look up the token by SHA-256 hash of the raw token")
        void shouldLookUpTokenByHash() {
            TokenHash expectedHash = TokenHash.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

            service.validate(new ValidateActivationTokenQuery(RAW_TOKEN));

            verify(tokenRepository).findByTokenHash(expectedHash);
        }

        @Test
        @DisplayName("should not mutate any aggregate (read-only)")
        void shouldNotMutateAnyAggregate() {
            TokenHash hash = TokenHash.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

            service.validate(new ValidateActivationTokenQuery(RAW_TOKEN));

            verify(tokenRepository, never()).save(any());
            verify(tokenRepository, never()).deleteByUserId(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ActivationTokenNotFoundException when no token matches the hash")
        void shouldThrowWhenTokenNotFound() {
            TokenHash hash = TokenHash.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.empty());

            assertThrows(ActivationTokenNotFoundException.class,
                    () -> service.validate(new ValidateActivationTokenQuery(RAW_TOKEN)));

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("should throw ActivationTokenExpiredException when token is expired")
        void shouldThrowWhenTokenExpired() {
            ActivationToken expired = ActivationToken.reconstitute(
                    ActivationTokenId.generate(),
                    user.id(),
                    TokenHash.of(RawToken.of(RAW_TOKEN)),
                    Instant.now().minus(Duration.ofHours(49)),
                    Instant.now().minus(Duration.ofHours(1))
            );
            TokenHash hash = TokenHash.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(expired));

            assertThrows(ActivationTokenExpiredException.class,
                    () -> service.validate(new ValidateActivationTokenQuery(RAW_TOKEN)));

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when token references a missing user (orphan)")
        void shouldThrowWhenUserOrphan() {
            TokenHash hash = TokenHash.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(user.id())).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> service.validate(new ValidateActivationTokenQuery(RAW_TOKEN)));
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null tokenRepository")
        void shouldRejectNullTokenRepository() {
            assertThrows(NullPointerException.class,
                    () -> new ValidateActivationTokenService(null, userRepository));
        }

        @Test
        @DisplayName("should reject null userRepository")
        void shouldRejectNullUserRepository() {
            assertThrows(NullPointerException.class,
                    () -> new ValidateActivationTokenService(tokenRepository, null));
        }

        @Test
        @DisplayName("should reject null query rawToken")
        void shouldRejectNullQueryRawToken() {
            assertThrows(NullPointerException.class,
                    () -> new ValidateActivationTokenQuery(null));
        }
    }
}
