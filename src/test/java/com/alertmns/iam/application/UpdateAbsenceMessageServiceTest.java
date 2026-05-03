package com.alertmns.iam.application;

import com.alertmns.iam.domain.event.AbsenceMessageUpdated;
import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.AbsenceMessage;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.iam.domain.port.incoming.command.UpdateAbsenceMessageCommand;
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

@DisplayName("UpdateAbsenceMessageService")
@ExtendWith(MockitoExtension.class)
class UpdateAbsenceMessageServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final String CONTENT = "Je ne suis pas disponible pour le moment";
    static final boolean ACTIVE = true;

    @Mock
    UserRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    UpdateAbsenceMessageService service;

    @Nested
    @DisplayName("AbsenceMessageUpdate")
    class AbsenceMessageUpdate {

        UserId id;
        User user;

        @BeforeEach
        void setUp() {
            id = UserId.generate();

            user = User.reconstitute(
                    id,
                    ORGANISATION_ID,
                    Email.of("johndoe@example.com"),
                    HashedPassword.of("$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"),
                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    UserRole.USER,
                    UserStatus.ACTIVE,
                    false,
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should save the user with the updated absence message")
        void shouldSaveTheUserWithUpdatedAbsenceMessage() {
            when(repository.findById(any())).thenReturn(Optional.of(user));

            UpdateAbsenceMessageCommand command = new UpdateAbsenceMessageCommand(
                    id.value().toString(), CONTENT, ACTIVE
            );

            service.update(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals(
                    Optional.of(AbsenceMessage.of(CONTENT, ACTIVE)),
                    saved.profile().absenceMessage()
            );
        }

        @Test
        @DisplayName("should publish AbsenceMessageUpdated event with the updated user id")
        void shouldPublishAbsenceMessageUpdatedEvent() {
            when(repository.findById(any())).thenReturn(Optional.of(user));

            UpdateAbsenceMessageCommand command = new UpdateAbsenceMessageCommand(
                    id.value().toString(), CONTENT, ACTIVE
            );

            service.update(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            AbsenceMessageUpdated event = assertInstanceOf(
                    AbsenceMessageUpdated.class, events.getFirst()
            );
            assertEquals(id, event.userId());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());

            UpdateAbsenceMessageCommand command = new UpdateAbsenceMessageCommand(
                    id.value().toString(), CONTENT, ACTIVE
            );

            assertThrows(UserNotFoundException.class, () -> service.update(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when userId is not a valid UUID")
        void shouldThrowWhenUserIdIsInvalid() {
            UpdateAbsenceMessageCommand command = new UpdateAbsenceMessageCommand(
                    "invalid", CONTENT, ACTIVE
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
                    () -> new UpdateAbsenceMessageService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new UpdateAbsenceMessageService(repository, null));
        }

        @Test
        @DisplayName("should reject null command userId")
        void shouldRejectNullCommandUserId() {
            assertThrows(NullPointerException.class,
                    () -> new UpdateAbsenceMessageCommand(null, CONTENT, ACTIVE));
        }

        @Test
        @DisplayName("should reject null command content")
        void shouldRejectNullCommandContent() {
            assertThrows(NullPointerException.class,
                    () -> new UpdateAbsenceMessageCommand(
                            UserId.generate().value().toString(), null, ACTIVE));
        }
    }
}
