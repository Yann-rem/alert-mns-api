package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.Role;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.iam.domain.port.incoming.command.UpdateAbsenceMessageCommand;
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

@DisplayName("UpdateAbsenceMessageService")
@ExtendWith(MockitoExtension.class)
class UpdateAbsenceMessageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UpdateAbsenceMessageService updateAbsenceMessageService;

    @Nested
    @DisplayName("Update")
    class Update {

        private UserId userId;
        private User user;

        @BeforeEach
        void setUp() {
            userId = UserId.generate();

            user = User.reconstitute(
                    userId,
                    Email.of("johndoe@example.com"),

                    HashedPassword.of(
                            "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"
                    ),

                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    Role.USER,
                    UserStatus.ACTIVE,
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should update an absence message")
        void shouldUpdateAnAbsenceMessage() {
            when(userRepository.findById(any())).thenReturn(Optional.of(user));

            UpdateAbsenceMessageCommand command = new UpdateAbsenceMessageCommand(
                    user.id().value().toString(),
                    "Je ne suis pas disponible pour le moment",
                    true
            );

            updateAbsenceMessageService.updateAbsenceMessage(command);
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publish(anyList());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            when(userRepository.findById(any())).thenReturn(Optional.empty());

            UpdateAbsenceMessageCommand command = new UpdateAbsenceMessageCommand(
                    user.id().value().toString(),
                    "Je ne suis pas disponible pour le moment",
                    true
            );

            assertThrows(UserNotFoundException.class,
                    () -> updateAbsenceMessageService.updateAbsenceMessage(command));

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
                    () -> new UpdateAbsenceMessageService(null, eventPublisher));
        }

        @Test
        @DisplayName("should reject null eventPublisher")
        void shouldRejectNullEventPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new UpdateAbsenceMessageService(userRepository, null));
        }
    }
}
