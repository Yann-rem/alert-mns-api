package com.alertmns.identity.application;

import com.alertmns.identity.domain.event.UserActivated;
import com.alertmns.identity.domain.exception.ActivationTokenExpiredException;
import com.alertmns.identity.domain.exception.ActivationTokenNotFoundException;
import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.identity.domain.model.ActivationToken;
import com.alertmns.identity.domain.model.ActivationTokenId;
import com.alertmns.identity.domain.model.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.HashedToken;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.RawPassword;
import com.alertmns.identity.domain.model.RawToken;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.command.RedeemActivationTokenCommand;
import com.alertmns.identity.domain.port.outgoing.ActivationTokenRepository;
import com.alertmns.identity.domain.port.outgoing.PasswordHasher;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("RedeemActivationTokenService")
@ExtendWith(MockitoExtension.class)
class RedeemActivationTokenServiceTest {

    static final String EMAIL = "johndoe@example.com";
    static final String FIRST_NAME = "John";
    static final String LAST_NAME = "Doe";
    static final String INITIAL_BCRYPT_HASH =
            "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";
    static final HashedPassword NEW_HASHED_PASSWORD = HashedPassword.of(
            "$2a$10$zzzzzzzzzzzzzzzzzzzzzzZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ987654");
    static final String RAW_TOKEN = "any-raw-token-value";
    static final String NEW_RAW_PASSWORD = "my-chosen-password";
    static final Duration TTL = Duration.ofHours(48);

    @Mock
    ActivationTokenRepository tokenRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordHasher passwordHasher;

    @Mock
    EventPublisher publisher;

    RedeemActivationTokenService service;
    User pendingUser;
    ActivationToken validToken;

    @BeforeEach
    void setUp() {
        service = new RedeemActivationTokenService(tokenRepository, userRepository, passwordHasher, publisher);
        pendingUser = User.register(
                Email.of(EMAIL),
                HashedPassword.of(INITIAL_BCRYPT_HASH),
                Profile.of(FirstName.of(FIRST_NAME), LastName.of(LAST_NAME))
        );
        // Simule l'état "user déjà en BD" — UserRegistered a déjà été publié à la création.
        // Sans ce flush, le redeem republierait UserRegistered + UserActivated, faussant les assertions
        // sur la taille de la liste d'events.
        pendingUser.pullDomainEvents();
        validToken = ActivationToken.issue(pendingUser.id(), RawToken.of(RAW_TOKEN), TTL).activationToken();
    }

    @Nested
    @DisplayName("Redeem")
    class Redeem {

        @Test
        @DisplayName("should activate the user and set the chosen password")
        void shouldActivateUserAndSetChosenPassword() {
            HashedToken hash = HashedToken.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByHash(hash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(pendingUser.id())).thenReturn(Optional.of(pendingUser));
            when(passwordHasher.hash(RawPassword.of(NEW_RAW_PASSWORD))).thenReturn(NEW_HASHED_PASSWORD);

            service.redeem(new RedeemActivationTokenCommand(RAW_TOKEN, NEW_RAW_PASSWORD));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals(UserStatus.ACTIVE, saved.status());
            assertEquals(NEW_HASHED_PASSWORD, saved.hashedPassword());
        }

        @Test
        @DisplayName("should delete the consumed token after saving the user")
        void shouldDeleteConsumedTokenAfterSavingUser() {
            HashedToken hash = HashedToken.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByHash(hash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(pendingUser.id())).thenReturn(Optional.of(pendingUser));
            when(passwordHasher.hash(RawPassword.of(NEW_RAW_PASSWORD))).thenReturn(NEW_HASHED_PASSWORD);

            service.redeem(new RedeemActivationTokenCommand(RAW_TOKEN, NEW_RAW_PASSWORD));

            var inOrder = inOrder(userRepository, tokenRepository);
            inOrder.verify(userRepository).save(any(User.class));
            inOrder.verify(tokenRepository).deleteByUserId(pendingUser.id());
        }

        @Test
        @DisplayName("should hash the raw password via the PasswordHasher port")
        void shouldHashRawPasswordViaPort() {
            HashedToken hash = HashedToken.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByHash(hash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(pendingUser.id())).thenReturn(Optional.of(pendingUser));
            when(passwordHasher.hash(RawPassword.of(NEW_RAW_PASSWORD))).thenReturn(NEW_HASHED_PASSWORD);

            service.redeem(new RedeemActivationTokenCommand(RAW_TOKEN, NEW_RAW_PASSWORD));

            verify(passwordHasher).hash(RawPassword.of(NEW_RAW_PASSWORD));
        }

        @Test
        @DisplayName("should publish UserActivated event after activating the user")
        void shouldPublishUserActivatedEvent() {
            HashedToken hash = HashedToken.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByHash(hash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(pendingUser.id())).thenReturn(Optional.of(pendingUser));
            when(passwordHasher.hash(RawPassword.of(NEW_RAW_PASSWORD))).thenReturn(NEW_HASHED_PASSWORD);

            service.redeem(new RedeemActivationTokenCommand(RAW_TOKEN, NEW_RAW_PASSWORD));

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            UserActivated event = assertInstanceOf(UserActivated.class, events.getFirst());
            assertEquals(pendingUser.id(), event.userId());
        }

        @Test
        @DisplayName("should throw ActivationTokenNotFoundException when token is unknown")
        void shouldThrowWhenTokenNotFound() {
            HashedToken hash = HashedToken.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByHash(hash)).thenReturn(Optional.empty());

            assertThrows(ActivationTokenNotFoundException.class,
                    () -> service.redeem(new RedeemActivationTokenCommand(RAW_TOKEN, NEW_RAW_PASSWORD)));

            verifyNoInteractions(userRepository, passwordHasher, publisher);
            verify(tokenRepository, never()).deleteByUserId(any());
        }

        @Test
        @DisplayName("should throw ActivationTokenExpiredException when token is expired")
        void shouldThrowWhenTokenExpired() {
            ActivationToken expired = ActivationToken.reconstitute(
                    ActivationTokenId.generate(),
                    pendingUser.id(),
                    HashedToken.of(RawToken.of(RAW_TOKEN)),
                    Instant.now().minus(Duration.ofHours(49)),
                    Instant.now().minus(Duration.ofHours(1))
            );
            HashedToken hash = HashedToken.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByHash(hash)).thenReturn(Optional.of(expired));

            assertThrows(ActivationTokenExpiredException.class,
                    () -> service.redeem(new RedeemActivationTokenCommand(RAW_TOKEN, NEW_RAW_PASSWORD)));

            verifyNoInteractions(userRepository, passwordHasher, publisher);
            verify(tokenRepository, never()).deleteByUserId(any());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when token references a missing user (orphan)")
        void shouldThrowWhenUserOrphan() {
            HashedToken hash = HashedToken.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByHash(hash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(pendingUser.id())).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> service.redeem(new RedeemActivationTokenCommand(RAW_TOKEN, NEW_RAW_PASSWORD)));

            verifyNoInteractions(passwordHasher);
            verify(userRepository, never()).save(any());
            verify(tokenRepository, never()).deleteByUserId(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalStateException when user is not PENDING (e.g. already ACTIVE)")
        void shouldThrowWhenUserNotPending() {
            User active = User.reconstitute(
                    pendingUser.id(),
                    Email.of(EMAIL),
                    HashedPassword.of(INITIAL_BCRYPT_HASH),
                    Profile.of(FirstName.of(FIRST_NAME), LastName.of(LAST_NAME)),
                    UserStatus.ACTIVE,
                    false,
                    Instant.now()
            );
            HashedToken hash = HashedToken.of(RawToken.of(RAW_TOKEN));
            when(tokenRepository.findByHash(hash)).thenReturn(Optional.of(validToken));
            when(userRepository.findById(pendingUser.id())).thenReturn(Optional.of(active));
            when(passwordHasher.hash(RawPassword.of(NEW_RAW_PASSWORD))).thenReturn(NEW_HASHED_PASSWORD);

            assertThrows(IllegalStateException.class,
                    () -> service.redeem(new RedeemActivationTokenCommand(RAW_TOKEN, NEW_RAW_PASSWORD)));

            verify(userRepository, never()).save(any());
            verify(tokenRepository, never()).deleteByUserId(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null tokenRepository")
        void shouldRejectNullTokenRepository() {
            assertThrows(NullPointerException.class,
                    () -> new RedeemActivationTokenService(null, userRepository, passwordHasher, publisher));
        }

        @Test
        @DisplayName("should reject null userRepository")
        void shouldRejectNullUserRepository() {
            assertThrows(NullPointerException.class,
                    () -> new RedeemActivationTokenService(tokenRepository, null, passwordHasher, publisher));
        }

        @Test
        @DisplayName("should reject null passwordHasher")
        void shouldRejectNullPasswordHasher() {
            assertThrows(NullPointerException.class,
                    () -> new RedeemActivationTokenService(tokenRepository, userRepository, null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new RedeemActivationTokenService(tokenRepository, userRepository, passwordHasher, null));
        }

        @Test
        @DisplayName("should reject null command rawToken")
        void shouldRejectNullCommandRawToken() {
            assertThrows(NullPointerException.class,
                    () -> new RedeemActivationTokenCommand(null, NEW_RAW_PASSWORD));
        }

        @Test
        @DisplayName("should reject null command rawPassword")
        void shouldRejectNullCommandRawPassword() {
            assertThrows(NullPointerException.class,
                    () -> new RedeemActivationTokenCommand(RAW_TOKEN, null));
        }
    }
}
