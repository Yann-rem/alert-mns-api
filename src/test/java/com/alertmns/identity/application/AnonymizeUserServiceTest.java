package com.alertmns.identity.application;

import com.alertmns.identity.domain.event.UserAnonymized;
import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.command.AnonymizeUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.Email;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AnonymizeUserService")
@ExtendWith(MockitoExtension.class)
class AnonymizeUserServiceTest {

    static final String BCRYPT_HASH = "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    UserRepository repository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    AnonymizeUserService service;

    @Nested
    @DisplayName("Anonymizing")
    class Anonymizing {

        UserId id;
        User activeUser;

        @BeforeEach
        void setUp() {
            id = UserId.generate();
            // lenient: some tests throw before reaching clock.instant() (UserNotFound, invalid UUID).
            lenient().when(clock.instant()).thenReturn(NOW);

            activeUser = User.reconstitute(
                    id,
                    Email.of("johndoe@example.com"),
                    HashedPassword.of(BCRYPT_HASH),
                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    UserStatus.ACTIVE,
                    false,
                    NOW
            );
        }

        @Test
        @DisplayName("should save the user with scrubbed PII and isAnonymized = true")
        void shouldSaveTheUserAnonymized() {
            when(repository.findById(any())).thenReturn(Optional.of(activeUser));
            AnonymizeUserCommand command = new AnonymizeUserCommand(id.value().toString());

            service.anonymize(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals(id, saved.id());
            assertTrue(saved.isAnonymized());
            assertEquals("anonymized-" + id.value() + "@deleted.local", saved.email().value());
            assertTrue(saved.hashedPassword().isUnset());
        }

        @Test
        @DisplayName("should publish UserAnonymized event with the anonymized user id")
        void shouldPublishUserAnonymizedEvent() {
            when(repository.findById(any())).thenReturn(Optional.of(activeUser));
            AnonymizeUserCommand command = new AnonymizeUserCommand(id.value().toString());

            service.anonymize(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            UserAnonymized event = assertInstanceOf(UserAnonymized.class, events.getFirst());
            assertEquals(id, event.userId());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("should publish no event when the user is already anonymized (idempotent)")
        void shouldPublishNoEventWhenAlreadyAnonymized() {
            User anonymized = User.reconstitute(
                    id,
                    Email.of("anonymized-" + id.value() + "@deleted.local"),
                    HashedPassword.unset(),
                    Profile.of(FirstName.of("Utilisateur"), LastName.of("Supprimé")),
                    UserStatus.ACTIVE,
                    true,
                    NOW
            );
            when(repository.findById(any())).thenReturn(Optional.of(anonymized));
            AnonymizeUserCommand command = new AnonymizeUserCommand(id.value().toString());

            service.anonymize(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            assertTrue(eventsCaptor.getValue().isEmpty());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());
            AnonymizeUserCommand command = new AnonymizeUserCommand(id.value().toString());
            assertThrows(UserNotFoundException.class, () -> service.anonymize(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when userId is not a valid UUID")
        void shouldThrowWhenUserIdIsInvalid() {
            AnonymizeUserCommand command = new AnonymizeUserCommand("invalid");
            assertThrows(IllegalArgumentException.class, () -> service.anonymize(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null repository")
        void shouldRejectNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new AnonymizeUserService(null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new AnonymizeUserService(repository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new AnonymizeUserService(repository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command userId")
        void shouldRejectNullCommandUserId() {
            assertThrows(NullPointerException.class,
                    () -> new AnonymizeUserCommand(null));
        }
    }
}
