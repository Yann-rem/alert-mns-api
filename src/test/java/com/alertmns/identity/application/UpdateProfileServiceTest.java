package com.alertmns.identity.application;

import com.alertmns.identity.domain.event.ProfileUpdated;
import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.shared.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.command.UpdateProfileCommand;
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

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UpdateProfileService")
@ExtendWith(MockitoExtension.class)
class UpdateProfileServiceTest {

    static final String NEW_FIRST_NAME = "Jane";
    static final String NEW_LAST_NAME = "Doe";
    static final String NEW_AVATAR = "https://cdn.example.com/avatar.jpg";
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    UserRepository repository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    UpdateProfileService service;

    @Nested
    @DisplayName("ProfileUpdate")
    class ProfileUpdate {

        UserId id;
        User user;

        @BeforeEach
        void setUp() {
            id = UserId.generate();
            lenient().when(clock.instant()).thenReturn(NOW);

            user = User.reconstitute(
                    id,
                    Email.of("johndoe@example.com"),
                    HashedPassword.of("$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"),
                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    UserStatus.ACTIVE,
                    false,
                    NOW
            );
        }

        @Test
        @DisplayName("should save the user with updated firstName, lastName and avatar")
        void shouldSaveTheUserWithUpdatedProfile() {
            when(repository.findById(any())).thenReturn(Optional.of(user));

            UpdateProfileCommand command = new UpdateProfileCommand(
                    id.value().toString(), NEW_FIRST_NAME, NEW_LAST_NAME, NEW_AVATAR
            );

            service.update(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals(FirstName.of(NEW_FIRST_NAME), saved.profile().firstName());
            assertEquals(LastName.of(NEW_LAST_NAME), saved.profile().lastName());
            assertEquals(Optional.of(NEW_AVATAR), saved.profile().avatar());
        }

        @Test
        @DisplayName("should publish ProfileUpdated event with the updated user id")
        void shouldPublishProfileUpdatedEvent() {
            when(repository.findById(any())).thenReturn(Optional.of(user));

            UpdateProfileCommand command = new UpdateProfileCommand(
                    id.value().toString(), NEW_FIRST_NAME, NEW_LAST_NAME, NEW_AVATAR
            );

            service.update(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            ProfileUpdated event = assertInstanceOf(ProfileUpdated.class, events.getFirst());
            assertEquals(id, event.userId());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());

            UpdateProfileCommand command = new UpdateProfileCommand(
                    id.value().toString(), NEW_FIRST_NAME, NEW_LAST_NAME, NEW_AVATAR
            );

            assertThrows(UserNotFoundException.class, () -> service.update(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when userId is not a valid UUID")
        void shouldThrowWhenUserIdIsInvalid() {
            UpdateProfileCommand command = new UpdateProfileCommand(
                    "invalid", NEW_FIRST_NAME, NEW_LAST_NAME, NEW_AVATAR
            );

            assertThrows(IllegalArgumentException.class, () -> service.update(command));
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
                    () -> new UpdateProfileService(null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new UpdateProfileService(repository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new UpdateProfileService(repository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command userId")
        void shouldRejectNullCommandUserId() {
            assertThrows(NullPointerException.class,
                    () -> new UpdateProfileCommand(
                            null, NEW_FIRST_NAME, NEW_LAST_NAME, NEW_AVATAR));
        }

        @Test
        @DisplayName("should reject null command firstName")
        void shouldRejectNullCommandFirstName() {
            assertThrows(NullPointerException.class,
                    () -> new UpdateProfileCommand(
                            UserId.generate().value().toString(), null, NEW_LAST_NAME, NEW_AVATAR));
        }

        @Test
        @DisplayName("should reject null command lastName")
        void shouldRejectNullCommandLastName() {
            assertThrows(NullPointerException.class,
                    () -> new UpdateProfileCommand(
                            UserId.generate().value().toString(), NEW_FIRST_NAME, null, NEW_AVATAR));
        }
    }
}
