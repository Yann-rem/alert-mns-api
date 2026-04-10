package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.EmailAlreadyExistsException;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.outgoing.AuthenticationPort;
import com.alertmns.iam.domain.port.outgoing.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RegisterUserService")
@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    AuthenticationPort authentication;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    RegisterUserService service;

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("should register a user")
        void shouldRegisterAUser() {
            when(repository.existsByEmail(any())).thenReturn(false);
            when(authentication.hashPassword(any())).thenReturn(
                    "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"
            );

            RegisterUserCommand command = new RegisterUserCommand(
                    "johndoe@example.com",
                    "changeme",
                    "John",
                    "Doe",
                    "550e8400-e29b-41d4-a716-446655440000"
            );

            service.register(command);
            verify(repository).save(any(User.class));
            verify(publisher).publish(anyList());
        }

        @Test
        @DisplayName("should throw EmailAlreadyExistsException when email already exists")
        void shouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
            when(repository.existsByEmail(any())).thenReturn(true);

            RegisterUserCommand command = new RegisterUserCommand(
                    "johndoe@example.com",
                    "changeme",
                    "John",
                    "Doe",
                    "550e8400-e29b-41d4-a716-446655440000"
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
                    () -> new RegisterUserService(null, authentication, publisher));
        }

        @Test
        @DisplayName("should reject null authentication")
        void shouldRejectNullAuthentication() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserService(repository, null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserService(repository, authentication, null));
        }
    }
}
