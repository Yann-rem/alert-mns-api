package com.alertmns.iam.application;

import com.alertmns.iam.domain.event.UserRegistered;
import com.alertmns.iam.domain.exception.EmailAlreadyExistsException;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.outgoing.PasswordHasher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RegisterUserService")
@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    static final String EMAIL = "johndoe@example.com";
    static final String RAW_PASSWORD = "changeme1234";
    static final HashedPassword HASHED_PASSWORD = HashedPassword.of(
            "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345");
    static final String FIRST_NAME = "John";
    static final String LAST_NAME = "Doe";
    static final String ORGANISATION_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    UserRepository repository;

    @Mock
    PasswordHasher passwordHasher;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    RegisterUserService service;

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("should save the user with email, profile, role USER, status PENDING and organisationId")
        void shouldSaveTheUserWithAllFields() {
            when(repository.existsByEmail(any())).thenReturn(false);
            when(passwordHasher.hash(any())).thenReturn(HASHED_PASSWORD);

            RegisterUserCommand command = new RegisterUserCommand(
                    EMAIL, RAW_PASSWORD, FIRST_NAME, LAST_NAME, ORGANISATION_ID
            );

            service.register(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals(Email.of(EMAIL), saved.email());
            assertEquals(FirstName.of(FIRST_NAME), saved.profile().firstName());
            assertEquals(LastName.of(LAST_NAME), saved.profile().lastName());
            assertEquals(UserRole.USER, saved.role());
            assertEquals(UserStatus.PENDING, saved.status());
            assertEquals(OrganisationId.from(ORGANISATION_ID), saved.organisationId());
        }

        @Test
        @DisplayName("should publish UserRegistered event with the saved user id")
        void shouldPublishUserRegisteredEvent() {
            when(repository.existsByEmail(any())).thenReturn(false);
            when(passwordHasher.hash(any())).thenReturn(HASHED_PASSWORD);

            RegisterUserCommand command = new RegisterUserCommand(
                    EMAIL, RAW_PASSWORD, FIRST_NAME, LAST_NAME, ORGANISATION_ID
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
        }

        @Test
        @DisplayName("should throw EmailAlreadyExistsException when email already exists")
        void shouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
            when(repository.existsByEmail(any())).thenReturn(true);

            RegisterUserCommand command = new RegisterUserCommand(
                    EMAIL, RAW_PASSWORD, FIRST_NAME, LAST_NAME, ORGANISATION_ID
            );

            assertThrows(EmailAlreadyExistsException.class, () -> service.register(command));
            verify(passwordHasher, never()).hash(any());
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when organisationId is not a valid UUID")
        void shouldThrowWhenOrganisationIdIsInvalid() {
            RegisterUserCommand command = new RegisterUserCommand(
                    EMAIL, RAW_PASSWORD, FIRST_NAME, LAST_NAME, "invalid"
            );

            assertThrows(IllegalArgumentException.class, () -> service.register(command));
            verify(repository, never()).existsByEmail(any());
            verify(passwordHasher, never()).hash(any());
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
                    () -> new RegisterUserService(null, passwordHasher, publisher));
        }

        @Test
        @DisplayName("should reject null passwordHasher")
        void shouldRejectNullPasswordHasher() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserService(repository, null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserService(repository, passwordHasher, null));
        }

        @Test
        @DisplayName("should reject null command email")
        void shouldRejectNullCommandEmail() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserCommand(
                            null, RAW_PASSWORD, FIRST_NAME, LAST_NAME, ORGANISATION_ID));
        }

        @Test
        @DisplayName("should reject null command rawPassword")
        void shouldRejectNullCommandRawPassword() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserCommand(
                            EMAIL, null, FIRST_NAME, LAST_NAME, ORGANISATION_ID));
        }

        @Test
        @DisplayName("should reject null command firstName")
        void shouldRejectNullCommandFirstName() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserCommand(
                            EMAIL, RAW_PASSWORD, null, LAST_NAME, ORGANISATION_ID));
        }

        @Test
        @DisplayName("should reject null command lastName")
        void shouldRejectNullCommandLastName() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserCommand(
                            EMAIL, RAW_PASSWORD, FIRST_NAME, null, ORGANISATION_ID));
        }

        @Test
        @DisplayName("should reject null command organisationId")
        void shouldRejectNullCommandOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserCommand(
                            EMAIL, RAW_PASSWORD, FIRST_NAME, LAST_NAME, null));
        }
    }
}
