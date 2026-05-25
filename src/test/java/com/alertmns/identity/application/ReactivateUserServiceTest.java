package com.alertmns.identity.application;

import com.alertmns.identity.domain.event.UserReactivated;
import com.alertmns.identity.domain.exception.BannedUserCannotBeReactivatedException;
import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.shared.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.command.ReactivateUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.DomainEvent;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ReactivateUserService")
@ExtendWith(MockitoExtension.class)
class ReactivateUserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    ReactivateUserService service;

    @Nested
    @DisplayName("Reactivation")
    class Reactivation {

        UserId id;
        User suspendedUser;

        @BeforeEach
        void setUp() {
            id = UserId.generate();

            suspendedUser = User.reconstitute(
                    id,
                    Email.of("johndoe@example.com"),
                    HashedPassword.of("$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"),
                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    UserStatus.SUSPENDED,
                    false,
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should save the user with status ACTIVE")
        void shouldSaveTheUserWithStatusActive() {
            when(repository.findById(any())).thenReturn(Optional.of(suspendedUser));
            ReactivateUserCommand command = new ReactivateUserCommand(id.value().toString());

            service.reactivate(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals(id, saved.id());
            assertEquals(UserStatus.ACTIVE, saved.status());
        }

        @Test
        @DisplayName("should publish UserReactivated event with the reactivated user id")
        void shouldPublishUserReactivatedEvent() {
            when(repository.findById(any())).thenReturn(Optional.of(suspendedUser));
            ReactivateUserCommand command = new ReactivateUserCommand(id.value().toString());

            service.reactivate(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            UserReactivated event = assertInstanceOf(UserReactivated.class, events.getFirst());
            assertEquals(id, event.userId());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());
            ReactivateUserCommand command = new ReactivateUserCommand(id.value().toString());
            assertThrows(UserNotFoundException.class, () -> service.reactivate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalStateException when user is not SUSPENDED")
        void shouldThrowIllegalStateExceptionWhenUserIsNotSUSPENDED() {
            User activeUser = User.reconstitute(
                    suspendedUser.id(),
                    suspendedUser.email(),
                    suspendedUser.hashedPassword(),
                    suspendedUser.profile(),
                    UserStatus.ACTIVE,
                    false,
                    Instant.now()
            );

            when(repository.findById(any())).thenReturn(Optional.of(activeUser));
            ReactivateUserCommand command = new ReactivateUserCommand(id.value().toString());
            assertThrows(IllegalStateException.class, () -> service.reactivate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should propagate BannedUserCannotBeReactivatedException when user is BANNED")
        void shouldPropagateBannedUserCannotBeReactivatedExceptionWhenUserIsBANNED() {
            User bannedUser = User.reconstitute(
                    suspendedUser.id(),
                    suspendedUser.email(),
                    suspendedUser.hashedPassword(),
                    suspendedUser.profile(),
                    UserStatus.BANNED,
                    false,
                    Instant.now()
            );

            when(repository.findById(any())).thenReturn(Optional.of(bannedUser));
            ReactivateUserCommand command = new ReactivateUserCommand(id.value().toString());
            assertThrows(BannedUserCannotBeReactivatedException.class, () -> service.reactivate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when userId is not a valid UUID")
        void shouldThrowWhenUserIdIsInvalid() {
            ReactivateUserCommand command = new ReactivateUserCommand("invalid");
            assertThrows(IllegalArgumentException.class, () -> service.reactivate(command));
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
                    () -> new ReactivateUserService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new ReactivateUserService(repository, null));
        }

        @Test
        @DisplayName("should reject null command userId")
        void shouldRejectNullCommandUserId() {
            assertThrows(NullPointerException.class,
                    () -> new ReactivateUserCommand(null));
        }
    }
}
