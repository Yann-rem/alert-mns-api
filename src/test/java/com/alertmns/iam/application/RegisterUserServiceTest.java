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
    UserRepository userRepository;

    @Mock
    AuthenticationPort authenticationPort;

    @Mock
    EventPublisher eventPublisher;

    @InjectMocks
    RegisterUserService registerUserService;

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("should register a user successfully")
        void shouldRegisterAUserSuccessfully() {
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(authenticationPort.hashPassword(any())).thenReturn(
                    "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"
            );

            RegisterUserCommand command = new RegisterUserCommand(
                    "johndoe@example.com",
                    "changeme",
                    "John",
                    "Doe"
            );

            registerUserService.register(command);
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publish(anyList());
        }

        @Test
        @DisplayName("should throw EmailAlreadyExistsException when email already exists")
        void shouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail(any())).thenReturn(true);

            RegisterUserCommand command = new RegisterUserCommand(
                    "johndoe@example.com",
                    "changeme",
                    "John",
                    "Doe"
            );

            assertThrows(EmailAlreadyExistsException.class, () -> registerUserService.register(command));
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null userRepository")
        void shouldRejectNullUserRepository() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserService(null, authenticationPort, eventPublisher));
        }

        @Test
        @DisplayName("should reject null authenticationPort")
        void shouldRejectNullAuthenticationPort() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserService(userRepository, null, eventPublisher));
        }

        @Test
        @DisplayName("should reject null eventPublisher")
        void shouldRejectNullEventPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new RegisterUserService(userRepository, authenticationPort, null));
        }
    }
}
