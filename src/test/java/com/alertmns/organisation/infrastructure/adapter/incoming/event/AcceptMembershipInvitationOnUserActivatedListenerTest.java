package com.alertmns.organisation.infrastructure.adapter.incoming.event;

import com.alertmns.identity.domain.event.UserActivated;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.model.MembershipInvitationStatus;
import com.alertmns.organisation.domain.port.incoming.AcceptMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.incoming.command.AcceptMembershipInvitationCommand;
import com.alertmns.organisation.domain.port.outgoing.MembershipInvitationRepository;
import com.alertmns.shared.Email;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AcceptMembershipInvitationOnUserActivatedListener")
@ExtendWith(MockitoExtension.class)
class AcceptMembershipInvitationOnUserActivatedListenerTest {

    private static final Email EMAIL = Email.of("activated@example.com");
    private static final Duration TTL = Duration.ofDays(7);

    @Mock
    MembershipInvitationRepository invitationRepository;

    @Mock
    AcceptMembershipInvitationUseCase acceptMembershipInvitationUseCase;

    @InjectMocks
    AcceptMembershipInvitationOnUserActivatedListener listener;

    private MembershipInvitation pendingInvitationFor(Email email) {
        Instant now = Instant.now();
        return MembershipInvitation.reconstitute(
                MembershipInvitationId.generate(),
                OrganisationId.generate(),
                email,
                MemberRole.MEMBER,
                MembershipInvitationStatus.PENDING,
                now,
                now.plus(TTL)
        );
    }

    @Nested
    @DisplayName("Cascade")
    class Cascade {

        @Test
        @DisplayName("should accept the matching invitation with the userId from the event")
        void shouldAcceptMatchingInvitationWithUserIdFromEvent() {
            UserId userId = UserId.generate();
            MembershipInvitation invitation = pendingInvitationFor(EMAIL);
            when(invitationRepository.findPendingByEmail(EMAIL)).thenReturn(Optional.of(invitation));

            listener.onUserActivatedEvent(new UserActivated(userId, EMAIL));

            ArgumentCaptor<AcceptMembershipInvitationCommand> commandCaptor =
                    ArgumentCaptor.forClass(AcceptMembershipInvitationCommand.class);
            verify(acceptMembershipInvitationUseCase).accept(commandCaptor.capture());
            AcceptMembershipInvitationCommand command = commandCaptor.getValue();
            assertThat(command.invitationId()).isEqualTo(invitation.id().value().toString());
            assertThat(command.userId()).isEqualTo(userId.value().toString());
        }

        @Test
        @DisplayName("should lookup the invitation by the email carried in the event")
        void shouldLookupTheInvitationByTheEmailFromTheEvent() {
            MembershipInvitation invitation = pendingInvitationFor(EMAIL);
            when(invitationRepository.findPendingByEmail(EMAIL)).thenReturn(Optional.of(invitation));

            listener.onUserActivatedEvent(new UserActivated(UserId.generate(), EMAIL));

            verify(invitationRepository).findPendingByEmail(EMAIL);
        }

        @Test
        @DisplayName("should throw IllegalStateException when no PENDING invitation matches the email")
        void shouldThrowWhenNoMatchingInvitation() {
            when(invitationRepository.findPendingByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class,
                    () -> listener.onUserActivatedEvent(new UserActivated(UserId.generate(), EMAIL)));

            verify(acceptMembershipInvitationUseCase, never()).accept(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null MembershipInvitationRepository")
        void shouldRejectNullInvitationRepository() {
            assertThrows(NullPointerException.class,
                    () -> new AcceptMembershipInvitationOnUserActivatedListener(
                            null, acceptMembershipInvitationUseCase));
        }

        @Test
        @DisplayName("should reject null AcceptMembershipInvitationUseCase")
        void shouldRejectNullAcceptUseCase() {
            assertThrows(NullPointerException.class,
                    () -> new AcceptMembershipInvitationOnUserActivatedListener(
                            invitationRepository, null));
        }
    }
}
