package com.alertmns.identity.application;

import com.alertmns.identity.domain.event.UserSuspended;
import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.identity.domain.model.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.command.SuspendUserCommand;
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

@DisplayName("SuspendUserService")
@ExtendWith(MockitoExtension.class)
class SuspendUserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    SuspendUserService service;

    @Nested
    @DisplayName("Suspending")
    class Suspending {

        UserId id;
        User activeUser;

        @BeforeEach
        void setUp() {
            id = UserId.generate();

            activeUser = User.reconstitute(
                    id,
                    Email.of("johndoe@example.com"),
                    HashedPassword.of("$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"),
                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    UserStatus.ACTIVE,
                    false,
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should save the user with status SUSPENDED")
        void shouldSaveTheUserWithStatusSuspended() {
            when(repository.findById(any())).thenReturn(Optional.of(activeUser));
            SuspendUserCommand command = new SuspendUserCommand(id.value().toString());

            service.suspend(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals(id, saved.id());
            assertEquals(UserStatus.SUSPENDED, saved.status());
        }

        @Test
        @DisplayName("should publish UserSuspended event with the suspended user id")
        void shouldPublishUserSuspendedEvent() {
            when(repository.findById(any())).thenReturn(Optional.of(activeUser));
            SuspendUserCommand command = new SuspendUserCommand(id.value().toString());

            service.suspend(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            UserSuspended event = assertInstanceOf(UserSuspended.class, events.getFirst());
            assertEquals(id, event.userId());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());
            SuspendUserCommand command = new SuspendUserCommand(id.value().toString());
            assertThrows(UserNotFoundException.class, () -> service.suspend(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalStateException when user is not ACTIVE")
        void shouldThrowIllegalStateExceptionWhenUserIsNotACTIVE() {
            User pendingUser = User.reconstitute(
                    activeUser.id(),
                    activeUser.email(),
                    activeUser.hashedPassword(),
                    activeUser.profile(),
                    UserStatus.PENDING,
                    false,
                    Instant.now()
            );

            when(repository.findById(any())).thenReturn(Optional.of(pendingUser));
            SuspendUserCommand command = new SuspendUserCommand(id.value().toString());
            assertThrows(IllegalStateException.class, () -> service.suspend(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when userId is not a valid UUID")
        void shouldThrowWhenUserIdIsInvalid() {
            SuspendUserCommand command = new SuspendUserCommand("invalid");
            assertThrows(IllegalArgumentException.class, () -> service.suspend(command));
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
                    () -> new SuspendUserService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new SuspendUserService(repository, null));
        }

        @Test
        @DisplayName("should reject null command userId")
        void shouldRejectNullCommandUserId() {
            assertThrows(NullPointerException.class,
                    () -> new SuspendUserCommand(null));
        }
    }
}
