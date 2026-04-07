package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.iam.domain.port.incoming.command.ActivateUserCommand;
import com.alertmns.iam.domain.port.outgoing.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ActivateUserService")
@ExtendWith(MockitoExtension.class)
class ActivateUserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    ActivateUserService service;

    @Nested
    @DisplayName("Activation")
    class Activation {

        UserId id;
        User pendingUser;

        @BeforeEach
        void setUp() {
            id = UserId.generate();

            pendingUser = User.reconstitute(
                    id,
                    Email.of("johndoe@example.com"),

                    HashedPassword.of(
                            "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"
                    ),

                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    UserRole.USER,
                    UserStatus.PENDING,
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should activate a user")
        void shouldActivateAUser() {
            when(repository.findById(any())).thenReturn(Optional.of(pendingUser));
            ActivateUserCommand command = new ActivateUserCommand(id.value().toString());
            service.activate(command);
            verify(repository).save(any(User.class));
            verify(publisher).publish(anyList());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());
            ActivateUserCommand command = new ActivateUserCommand(id.value().toString());
            assertThrows(UserNotFoundException.class, () -> service.activate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalStateException when user is not PENDING")
        void shouldThrowIllegalStateExceptionWhenUserIsNotPENDING() {
            User activeUser = User.reconstitute(
                    pendingUser.id(),
                    pendingUser.email(),
                    pendingUser.hashedPassword(),
                    pendingUser.profile(),
                    pendingUser.role(),
                    UserStatus.ACTIVE,
                    Instant.now()
            );

            when(repository.findById(any())).thenReturn(Optional.of(activeUser));
            ActivateUserCommand command = new ActivateUserCommand(id.value().toString());
            assertThrows(IllegalStateException.class, () -> service.activate(command));
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
                    () -> new ActivateUserService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new ActivateUserService(repository, null));
        }
    }
}
