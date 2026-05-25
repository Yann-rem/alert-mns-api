package com.alertmns.identity.infrastructure.adapter.incoming.event;

import com.alertmns.identity.domain.event.UserRegistered;
import com.alertmns.identity.domain.model.Email;
import com.alertmns.identity.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.command.IssueActivationTokenCommand;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@DisplayName("IssueActivationTokenOnUserRegisteredListener")
@ExtendWith(MockitoExtension.class)
class IssueActivationTokenOnUserRegisteredListenerTest {

    @Mock
    IssueActivationTokenUseCase issueActivationTokenUseCase;

    @InjectMocks
    IssueActivationTokenOnUserRegisteredListener listener;

    @Nested
    @DisplayName("Cascade")
    class Cascade {

        @Test
        @DisplayName("should issue an activation token when UserRegistered event is received")
        void shouldIssueActivationTokenWhenUserRegisteredEventIsReceived() {
            UserId userId = UserId.generate();
            UserRegistered event = new UserRegistered(
                    userId,
                    Email.of("johndoe@example.com")
            );

            listener.onUserRegistered(event);

            verify(issueActivationTokenUseCase).issue(
                    new IssueActivationTokenCommand(userId.value().toString())
            );
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null IssueActivationTokenUseCase")
        void shouldRejectNullUseCase() {
            assertThrows(NullPointerException.class,
                    () -> new IssueActivationTokenOnUserRegisteredListener(null));
        }
    }
}
