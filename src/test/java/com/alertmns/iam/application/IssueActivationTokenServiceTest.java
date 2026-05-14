package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.ActivationToken;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.RawToken;
import com.alertmns.iam.domain.model.TokenHash;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.incoming.command.IssueActivationTokenCommand;
import com.alertmns.iam.domain.port.outgoing.ActivationTokenRepository;
import com.alertmns.iam.domain.port.outgoing.MailerPort;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("IssueActivationTokenService")
@ExtendWith(MockitoExtension.class)
class IssueActivationTokenServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final String EMAIL = "johndoe@example.com";
    static final String FIRST_NAME = "John";
    static final String LAST_NAME = "Doe";
    static final String BCRYPT_HASH = "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";
    static final Duration TTL = Duration.ofHours(48);
    static final URI FRONTEND_BASE_URL = URI.create("https://app.alertmns.com/");

    @Mock
    ActivationTokenRepository tokenRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MailerPort mailer;

    IssueActivationTokenService service;
    User user;

    @BeforeEach
    void setUp() {
        service = new IssueActivationTokenService(
                tokenRepository, userRepository, mailer, TTL, FRONTEND_BASE_URL
        );
        user = User.register(
                ORGANISATION_ID,
                Email.of(EMAIL),
                HashedPassword.of(BCRYPT_HASH),
                Profile.of(FirstName.of(FIRST_NAME), LastName.of(LAST_NAME))
        );
    }

    @Nested
    @DisplayName("Issuance")
    class Issuance {

        @Test
        @DisplayName("should save a token bound to the target user")
        void shouldSaveATokenBoundToTheTargetUser() {
            when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

            service.issue(new IssueActivationTokenCommand(user.id().value().toString()));

            ArgumentCaptor<ActivationToken> tokenCaptor = ArgumentCaptor.forClass(ActivationToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());
            assertEquals(user.id(), tokenCaptor.getValue().userId());
        }

        @Test
        @DisplayName("should delete previous tokens before saving the new one")
        void shouldDeletePreviousTokensBeforeSavingTheNewOne() {
            when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

            service.issue(new IssueActivationTokenCommand(user.id().value().toString()));

            var inOrder = inOrder(tokenRepository);
            inOrder.verify(tokenRepository).deleteByUserId(user.id());
            inOrder.verify(tokenRepository).save(any(ActivationToken.class));
        }

        @Test
        @DisplayName("should set expiresAt to createdAt + ttl on the saved token")
        void shouldSetExpiresAtToCreatedAtPlusTtl() {
            when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

            service.issue(new IssueActivationTokenCommand(user.id().value().toString()));

            ArgumentCaptor<ActivationToken> tokenCaptor = ArgumentCaptor.forClass(ActivationToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());
            ActivationToken saved = tokenCaptor.getValue();
            assertEquals(saved.createdAt().plus(TTL), saved.expiresAt());
        }

        @Test
        @DisplayName("should send an activation email to the user with the magic link")
        void shouldSendActivationEmail() {
            when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

            service.issue(new IssueActivationTokenCommand(user.id().value().toString()));

            ArgumentCaptor<URI> linkCaptor = ArgumentCaptor.forClass(URI.class);
            verify(mailer).sendActivationEmail(
                    eq(user.email()),
                    eq(user.profile().firstName()),
                    linkCaptor.capture()
            );
            URI link = linkCaptor.getValue();
            assertTrue(link.toString().startsWith("https://app.alertmns.com/activate?token="),
                    "link should be built from frontendBaseUrl with /activate path, but was: " + link);
        }

        @Test
        @DisplayName("the saved token hash should match the raw token in the magic link")
        void savedTokenHashShouldMatchRawTokenInLink() {
            when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

            service.issue(new IssueActivationTokenCommand(user.id().value().toString()));

            ArgumentCaptor<URI> linkCaptor = ArgumentCaptor.forClass(URI.class);
            verify(mailer).sendActivationEmail(any(), any(), linkCaptor.capture());
            ArgumentCaptor<ActivationToken> tokenCaptor = ArgumentCaptor.forClass(ActivationToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            String rawTokenValue = linkCaptor.getValue().getQuery().substring("token=".length());
            TokenHash expected = TokenHash.of(RawToken.of(rawTokenValue));
            assertEquals(expected, tokenCaptor.getValue().tokenHash());
        }

        @Test
        @DisplayName("should generate a different raw token on each call")
        void shouldGenerateDifferentRawTokensOnEachCall() {
            when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

            service.issue(new IssueActivationTokenCommand(user.id().value().toString()));
            service.issue(new IssueActivationTokenCommand(user.id().value().toString()));

            ArgumentCaptor<URI> linkCaptor = ArgumentCaptor.forClass(URI.class);
            verify(mailer, times(2)).sendActivationEmail(any(), any(), linkCaptor.capture());
            String firstToken = linkCaptor.getAllValues().get(0).getQuery().substring("token=".length());
            String secondToken = linkCaptor.getAllValues().get(1).getQuery().substring("token=".length());
            assertNotEquals(firstToken, secondToken);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            UserId unknown = UserId.generate();
            when(userRepository.findById(unknown)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> service.issue(new IssueActivationTokenCommand(unknown.value().toString())));

            verify(tokenRepository, never()).save(any());
            verify(tokenRepository, never()).deleteByUserId(any());
            verify(mailer, never()).sendActivationEmail(any(), any(), any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when command userId is not a valid UUID")
        void shouldThrowWhenUserIdIsInvalid() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.issue(new IssueActivationTokenCommand("invalid")));

            verify(userRepository, never()).findById(any());
            verify(tokenRepository, never()).save(any());
            verify(mailer, never()).sendActivationEmail(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null tokenRepository")
        void shouldRejectNullTokenRepository() {
            assertThrows(NullPointerException.class,
                    () -> new IssueActivationTokenService(null, userRepository, mailer, TTL, FRONTEND_BASE_URL));
        }

        @Test
        @DisplayName("should reject null userRepository")
        void shouldRejectNullUserRepository() {
            assertThrows(NullPointerException.class,
                    () -> new IssueActivationTokenService(tokenRepository, null, mailer, TTL, FRONTEND_BASE_URL));
        }

        @Test
        @DisplayName("should reject null mailer")
        void shouldRejectNullMailer() {
            assertThrows(NullPointerException.class,
                    () -> new IssueActivationTokenService(tokenRepository, userRepository, null, TTL, FRONTEND_BASE_URL));
        }

        @Test
        @DisplayName("should reject null ttl")
        void shouldRejectNullTtl() {
            assertThrows(NullPointerException.class,
                    () -> new IssueActivationTokenService(tokenRepository, userRepository, mailer, null, FRONTEND_BASE_URL));
        }

        @Test
        @DisplayName("should reject null frontendBaseUrl")
        void shouldRejectNullFrontendBaseUrl() {
            assertThrows(NullPointerException.class,
                    () -> new IssueActivationTokenService(tokenRepository, userRepository, mailer, TTL, null));
        }

        @Test
        @DisplayName("should reject null command userId")
        void shouldRejectNullCommandUserId() {
            assertThrows(NullPointerException.class,
                    () -> new IssueActivationTokenCommand(null));
        }
    }
}
