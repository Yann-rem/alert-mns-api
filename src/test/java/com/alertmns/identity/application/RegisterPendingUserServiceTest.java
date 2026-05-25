package com.alertmns.identity.application;

import com.alertmns.identity.domain.event.UserRegistered;
import com.alertmns.identity.domain.exception.EmailAlreadyExistsException;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.command.RegisterPendingUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.Email;
import com.alertmns.shared.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RegisterPendingUserService")
@ExtendWith(MockitoExtension.class)
class RegisterPendingUserServiceTest {

    static final String EMAIL = "johndoe@example.com";
    static final String FIRST_NAME = "John";
    static final String LAST_NAME = "Doe";

    @Mock
    UserRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    RegisterPendingUserService service;

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("should save the user with email, profile, and status PENDING")
        void shouldSaveTheUserWithAllFields() {
            when(repository.existsByEmail(any())).thenReturn(false);

            RegisterPendingUserCommand command = new RegisterPendingUserCommand(
                    EMAIL, FIRST_NAME, LAST_NAME
            );

            service.register(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals(Email.of(EMAIL), saved.email());
            assertEquals(FirstName.of(FIRST_NAME), saved.profile().firstName());
            assertEquals(LastName.of(LAST_NAME), saved.profile().lastName());
            assertEquals(UserStatus.PENDING, saved.status());
        }

        @Test
        @DisplayName("should save the user with HashedPassword.unset() sentinel (no real password generated)")
        void shouldSaveTheUserWithUnsetSentinel() {
            when(repository.existsByEmail(any())).thenReturn(false);

            RegisterPendingUserCommand command = new RegisterPendingUserCommand(
                    EMAIL, FIRST_NAME, LAST_NAME
            );

            service.register(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertTrue(saved.hashedPassword().isUnset(),
                    "Le User invité doit avoir un HashedPassword sentinel, pas un hash réel");
            assertEquals(HashedPassword.unset(), saved.hashedPassword());
        }

        @Test
        @DisplayName("should publish UserRegistered event with the saved user id")
        void shouldPublishUserRegisteredEvent() {
            when(repository.existsByEmail(any())).thenReturn(false);

            RegisterPendingUserCommand command = new RegisterPendingUserCommand(
                    EMAIL, FIRST_NAME, LAST_NAME
            );

            service.register(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            UserRegistered event = assertInstanceOf(UserRegistered.class, events.getFirst());
            assertEquals(saved.id(), event.userId());
            assertEquals(saved.email(), event.email());
        }

        @Test
        @DisplayName("should return the id of the newly created user")
        void shouldReturnTheIdOfTheNewlyCreatedUser() {
            when(repository.existsByEmail(any())).thenReturn(false);

            RegisterPendingUserCommand command = new RegisterPendingUserCommand(
                    EMAIL, FIRST_NAME, LAST_NAME
            );

            var returnedId = service.register(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            assertThat(returnedId).isEqualTo(userCaptor.getValue().id());
        }

        @Test
        @DisplayName("should throw EmailAlreadyExistsException when email already exists")
        void shouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
            when(repository.existsByEmail(any())).thenReturn(true);

            RegisterPendingUserCommand command = new RegisterPendingUserCommand(
                    EMAIL, FIRST_NAME, LAST_NAME
            );

            assertThrows(EmailAlreadyExistsException.class, () -> service.register(command));
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
                    () -> new RegisterPendingUserService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterPendingUserService(repository, null));
        }

        @Test
        @DisplayName("should reject null command email")
        void shouldRejectNullCommandEmail() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterPendingUserCommand(null, FIRST_NAME, LAST_NAME));
        }

        @Test
        @DisplayName("should reject null command firstName")
        void shouldRejectNullCommandFirstName() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterPendingUserCommand(EMAIL, null, LAST_NAME));
        }

        @Test
        @DisplayName("should reject null command lastName")
        void shouldRejectNullCommandLastName() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterPendingUserCommand(EMAIL, FIRST_NAME, null));
        }
    }
}
