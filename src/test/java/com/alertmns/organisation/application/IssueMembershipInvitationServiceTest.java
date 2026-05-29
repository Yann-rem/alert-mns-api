package com.alertmns.organisation.application;

import com.alertmns.identity.domain.port.incoming.RegisterPendingUserUseCase;
import com.alertmns.identity.domain.port.incoming.command.RegisterPendingUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.event.MembershipInvitationIssued;
import com.alertmns.organisation.domain.exception.InvitationAlreadyPendingException;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.model.MembershipInvitationStatus;
import com.alertmns.organisation.domain.port.incoming.command.IssueMembershipInvitationCommand;
import com.alertmns.organisation.domain.port.outgoing.MembershipInvitationRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.Email;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("IssueMembershipInvitationService")
@ExtendWith(MockitoExtension.class)
class IssueMembershipInvitationServiceTest {

    private static final String ORGANISATION_ID = "00000000-0000-0000-0000-000000000001";
    private static final String INVITED_EMAIL = "invited@example.com";
    private static final String FIRST_NAME = "Alice";
    private static final String LAST_NAME = "Doe";
    private static final String ROLE = "MEMBER";
    private static final Duration TTL = Duration.ofDays(7);

    @Mock
    MembershipInvitationRepository invitationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    RegisterPendingUserUseCase registerPendingUserUseCase;

    @Mock
    EventPublisher publisher;

    IssueMembershipInvitationService service;

    @BeforeEach
    void setUp() {
        service = new IssueMembershipInvitationService(
                invitationRepository, userRepository, registerPendingUserUseCase, publisher, TTL);
    }

    private IssueMembershipInvitationCommand command() {
        return new IssueMembershipInvitationCommand(
                ORGANISATION_ID, INVITED_EMAIL, FIRST_NAME, LAST_NAME, ROLE);
    }

    @Nested
    @DisplayName("Issuance — happy path (case A)")
    class HappyPath {

        @Test
        @DisplayName("should save the invitation with the configured fields and TTL")
        void shouldSaveTheInvitationWithCorrectFields() {
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(invitationRepository.existsPendingByEmail(any())).thenReturn(false);

            service.issue(command());

            ArgumentCaptor<MembershipInvitation> invitationCaptor =
                    ArgumentCaptor.forClass(MembershipInvitation.class);
            verify(invitationRepository).save(invitationCaptor.capture());
            MembershipInvitation saved = invitationCaptor.getValue();

            assertEquals(OrganisationId.from(ORGANISATION_ID), saved.organisationId());
            assertEquals(Email.of(INVITED_EMAIL), saved.invitedEmail());
            assertEquals(MemberRole.MEMBER, saved.role());
            assertEquals(MembershipInvitationStatus.PENDING, saved.status());
            assertEquals(TTL, Duration.between(saved.createdAt(), saved.expiresAt()));
        }

        @Test
        @DisplayName("should orchestrate RegisterPendingUserUseCase with the right command")
        void shouldOrchestrateRegisterPendingUser() {
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(invitationRepository.existsPendingByEmail(any())).thenReturn(false);

            service.issue(command());

            ArgumentCaptor<RegisterPendingUserCommand> registerCaptor =
                    ArgumentCaptor.forClass(RegisterPendingUserCommand.class);
            verify(registerPendingUserUseCase).register(registerCaptor.capture());
            RegisterPendingUserCommand registerCommand = registerCaptor.getValue();

            assertEquals(INVITED_EMAIL, registerCommand.email());
            assertEquals(FIRST_NAME, registerCommand.firstName());
            assertEquals(LAST_NAME, registerCommand.lastName());
        }

        @Test
        @DisplayName("should publish MembershipInvitationIssued with the invitation id")
        void shouldPublishMembershipInvitationIssued() {
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(invitationRepository.existsPendingByEmail(any())).thenReturn(false);

            MembershipInvitationId returnedId = service.issue(command());

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            MembershipInvitationIssued event = assertInstanceOf(
                    MembershipInvitationIssued.class, events.getFirst());
            assertEquals(returnedId, event.invitationId());
            assertEquals(OrganisationId.from(ORGANISATION_ID), event.organisationId());
            assertEquals(Email.of(INVITED_EMAIL), event.invitedEmail());
            assertEquals(MemberRole.MEMBER, event.role());
        }

        @Test
        @DisplayName("should return the id of the newly created invitation")
        void shouldReturnTheIdOfTheNewlyCreatedInvitation() {
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(invitationRepository.existsPendingByEmail(any())).thenReturn(false);

            MembershipInvitationId returnedId = service.issue(command());

            ArgumentCaptor<MembershipInvitation> invitationCaptor =
                    ArgumentCaptor.forClass(MembershipInvitation.class);
            verify(invitationRepository).save(invitationCaptor.capture());
            assertThat(returnedId).isEqualTo(invitationCaptor.getValue().id());
        }
    }

    @Nested
    @DisplayName("Case B detection")
    class CaseBDetection {

        @Test
        @DisplayName("should throw UnsupportedOperationException when the email matches an existing user")
        void shouldThrowWhenUserAlreadyExists() {
            when(userRepository.existsByEmail(Email.of(INVITED_EMAIL))).thenReturn(true);

            assertThrows(UnsupportedOperationException.class, () -> service.issue(command()));
        }

        @Test
        @DisplayName("should not orchestrate any side effect when case B is detected")
        void shouldNotOrchestrateAnySideEffectWhenCaseB() {
            when(userRepository.existsByEmail(any())).thenReturn(true);

            assertThrows(UnsupportedOperationException.class, () -> service.issue(command()));

            verify(invitationRepository, never()).existsPendingByEmail(any());
            verify(registerPendingUserUseCase, never()).register(any());
            verify(invitationRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Double invitation detection")
    class DoubleInvitationDetection {

        @Test
        @DisplayName("should throw InvitationAlreadyPendingException when a PENDING invitation exists for the email")
        void shouldThrowWhenInvitationAlreadyPending() {
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(invitationRepository.existsPendingByEmail(Email.of(INVITED_EMAIL))).thenReturn(true);

            assertThrows(InvitationAlreadyPendingException.class, () -> service.issue(command()));
        }

        @Test
        @DisplayName("should not orchestrate any side effect when invitation already PENDING")
        void shouldNotOrchestrateAnySideEffectWhenAlreadyPending() {
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(invitationRepository.existsPendingByEmail(any())).thenReturn(true);

            assertThrows(InvitationAlreadyPendingException.class, () -> service.issue(command()));

            verify(registerPendingUserUseCase, never()).register(any());
            verify(invitationRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null invitationRepository")
        void shouldRejectNullInvitationRepository() {
            assertThrows(NullPointerException.class, () -> new IssueMembershipInvitationService(
                    null, userRepository, registerPendingUserUseCase, publisher, TTL));
        }

        @Test
        @DisplayName("should reject null userRepository")
        void shouldRejectNullUserRepository() {
            assertThrows(NullPointerException.class, () -> new IssueMembershipInvitationService(
                    invitationRepository, null, registerPendingUserUseCase, publisher, TTL));
        }

        @Test
        @DisplayName("should reject null registerPendingUserUseCase")
        void shouldRejectNullRegisterPendingUserUseCase() {
            assertThrows(NullPointerException.class, () -> new IssueMembershipInvitationService(
                    invitationRepository, userRepository, null, publisher, TTL));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class, () -> new IssueMembershipInvitationService(
                    invitationRepository, userRepository, registerPendingUserUseCase, null, TTL));
        }

        @Test
        @DisplayName("should reject null ttl")
        void shouldRejectNullTtl() {
            assertThrows(NullPointerException.class, () -> new IssueMembershipInvitationService(
                    invitationRepository, userRepository, registerPendingUserUseCase, publisher, null));
        }
    }

    @Nested
    @DisplayName("Parsing errors")
    class ParsingErrors {

        @Test
        @DisplayName("should propagate IllegalArgumentException when role is invalid")
        void shouldPropagateIllegalArgumentExceptionWhenRoleIsInvalid() {
            // No stubbing : the parse happens before any repository interaction.
            IssueMembershipInvitationCommand badRole = new IssueMembershipInvitationCommand(
                    ORGANISATION_ID, INVITED_EMAIL, FIRST_NAME, LAST_NAME, "FOO");

            assertThrows(IllegalArgumentException.class, () -> service.issue(badRole));
            verifyNoInteractions(userRepository, invitationRepository, registerPendingUserUseCase, publisher);
        }

        @Test
        @DisplayName("should propagate IllegalArgumentException when organisationId is not a valid UUID")
        void shouldPropagateIllegalArgumentExceptionWhenOrganisationIdInvalid() {
            IssueMembershipInvitationCommand badOrgId = new IssueMembershipInvitationCommand(
                    "not-a-uuid", INVITED_EMAIL, FIRST_NAME, LAST_NAME, ROLE);

            assertThrows(IllegalArgumentException.class, () -> service.issue(badOrgId));
            verifyNoInteractions(userRepository, invitationRepository, registerPendingUserUseCase, publisher);
        }
    }

}
