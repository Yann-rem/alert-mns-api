package com.alertmns.iam.application;

import com.alertmns.iam.domain.event.UserDisabled;
import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.iam.domain.port.incoming.command.DisableUserCommand;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;
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

@DisplayName("DisableUserService")
@ExtendWith(MockitoExtension.class)
class DisableUserServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();

    @Mock
    UserRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    DisableUserService service;

    @Nested
    @DisplayName("Disabling")
    class Disabling {

        UserId id;
        User activeUser;

        @BeforeEach
        void setUp() {
            id = UserId.generate();

            activeUser = User.reconstitute(
                    id,
                    ORGANISATION_ID,
                    Email.of("johndoe@example.com"),
                    HashedPassword.of("$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"),
                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    UserRole.USER,
                    UserStatus.ACTIVE,
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should save the user with status DISABLED")
        void shouldSaveTheUserWithStatusDisabled() {
            when(repository.findById(any())).thenReturn(Optional.of(activeUser));
            DisableUserCommand command = new DisableUserCommand(id.value().toString());

            service.disable(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals(id, saved.id());
            assertEquals(UserStatus.DISABLED, saved.status());
        }

        @Test
        @DisplayName("should publish UserDisabled event with the disabled user id")
        void shouldPublishUserDisabledEvent() {
            when(repository.findById(any())).thenReturn(Optional.of(activeUser));
            DisableUserCommand command = new DisableUserCommand(id.value().toString());

            service.disable(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            UserDisabled event = assertInstanceOf(UserDisabled.class, events.getFirst());
            assertEquals(id, event.userId());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());
            DisableUserCommand command = new DisableUserCommand(id.value().toString());
            assertThrows(UserNotFoundException.class, () -> service.disable(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalStateException when user is not ACTIVE")
        void shouldThrowIllegalStateExceptionWhenUserIsNotACTIVE() {
            User pendingUser = User.reconstitute(
                    activeUser.id(),
                    activeUser.organisationId(),
                    activeUser.email(),
                    activeUser.hashedPassword(),
                    activeUser.profile(),
                    activeUser.role(),
                    UserStatus.PENDING,
                    Instant.now()
            );

            when(repository.findById(any())).thenReturn(Optional.of(pendingUser));
            DisableUserCommand command = new DisableUserCommand(id.value().toString());
            assertThrows(IllegalStateException.class, () -> service.disable(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when userId is not a valid UUID")
        void shouldThrowWhenUserIdIsInvalid() {
            DisableUserCommand command = new DisableUserCommand("invalid");
            assertThrows(IllegalArgumentException.class, () -> service.disable(command));
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
                    () -> new DisableUserService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new DisableUserService(repository, null));
        }

        @Test
        @DisplayName("should reject null command userId")
        void shouldRejectNullCommandUserId() {
            assertThrows(NullPointerException.class,
                    () -> new DisableUserCommand(null));
        }
    }
}
