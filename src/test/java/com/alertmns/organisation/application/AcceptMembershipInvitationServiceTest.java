package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.MemberJoined;
import com.alertmns.organisation.domain.event.MembershipInvitationAccepted;
import com.alertmns.organisation.domain.exception.InvitationExpiredException;
import com.alertmns.organisation.domain.exception.MembershipInvitationNotFoundException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.model.MembershipInvitationStatus;
import com.alertmns.organisation.domain.port.incoming.command.AcceptMembershipInvitationCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AcceptMembershipInvitationService")
@ExtendWith(MockitoExtension.class)
class AcceptMembershipInvitationServiceTest {

    private static final MembershipInvitationId INVITATION_ID = MembershipInvitationId.generate();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final OrganisationId ORG_ID = OrganisationId.generate();
    private static final Email INVITED_EMAIL = Email.of("invited@example.com");
    private static final Duration TTL = Duration.ofDays(7);

    @Mock
    MembershipInvitationRepository invitationRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    AcceptMembershipInvitationService service;

    AcceptMembershipInvitationCommand command;

    @BeforeEach
    void setUp() {
        command = new AcceptMembershipInvitationCommand(
                INVITATION_ID.value().toString(), USER_ID.toString());
    }

    private MembershipInvitation pendingInvitation(MemberRole role) {
        Instant now = Instant.now();
        MembershipInvitation invitation = MembershipInvitation.reconstitute(
                INVITATION_ID, ORG_ID, INVITED_EMAIL, role,
                MembershipInvitationStatus.PENDING,
                now, now.plus(TTL)
        );
        // No event to clear : reconstitute does not emit.
        return invitation;
    }

    @Nested
    @DisplayName("Acceptance — happy path")
    class HappyPath {

        @Test
        @DisplayName("should accept the invitation and save it in ACCEPTED status")
        void shouldAcceptAndSaveTheInvitation() {
            MembershipInvitation invitation = pendingInvitation(MemberRole.MEMBER);
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(invitation));

            service.accept(command);

            ArgumentCaptor<MembershipInvitation> invitationCaptor =
                    ArgumentCaptor.forClass(MembershipInvitation.class);
            verify(invitationRepository).save(invitationCaptor.capture());
            assertEquals(MembershipInvitationStatus.ACCEPTED, invitationCaptor.getValue().status());
        }

        @Test
        @DisplayName("should create a Member ACTIVE for the (orgId, userId, role) tuple from the invitation")
        void shouldCreateMemberActiveFromInvitation() {
            MembershipInvitation invitation = pendingInvitation(MemberRole.ADMIN);
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(invitation));

            service.accept(command);

            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository).save(memberCaptor.capture());
            Member savedMember = memberCaptor.getValue();
            assertEquals(ORG_ID, savedMember.organisationId());
            assertEquals(USER_ID, savedMember.userId());
            assertEquals(MemberRole.ADMIN, savedMember.role());
            assertEquals(MemberStatus.ACTIVE, savedMember.status());
        }

        @Test
        @DisplayName("should publish MembershipInvitationAccepted and MemberJoined in one batch")
        void shouldPublishBothEventsInOneBatch() {
            MembershipInvitation invitation = pendingInvitation(MemberRole.MEMBER);
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(invitation));

            service.accept(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertThat(events).hasSize(2);
            assertThat(events).hasAtLeastOneElementOfType(MembershipInvitationAccepted.class);
            assertThat(events).hasAtLeastOneElementOfType(MemberJoined.class);
        }

        @Test
        @DisplayName("MemberJoined should carry organisationId, memberId, userId and role")
        void memberJoinedShouldCarryContext() {
            MembershipInvitation invitation = pendingInvitation(MemberRole.ADMIN);
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(invitation));

            service.accept(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            MemberJoined event = eventsCaptor.getValue().stream()
                    .filter(MemberJoined.class::isInstance)
                    .map(MemberJoined.class::cast)
                    .findFirst().orElseThrow();
            assertEquals(ORG_ID, event.organisationId());
            assertEquals(USER_ID, event.userId());
            assertEquals(MemberRole.ADMIN, event.role());
        }

        @Test
        @DisplayName("MembershipInvitationAccepted should carry userId, role and orgId from the invitation")
        void membershipInvitationAcceptedShouldCarryContext() {
            MembershipInvitation invitation = pendingInvitation(MemberRole.ADMIN);
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(invitation));

            service.accept(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            MembershipInvitationAccepted event = eventsCaptor.getValue().stream()
                    .filter(MembershipInvitationAccepted.class::isInstance)
                    .map(MembershipInvitationAccepted.class::cast)
                    .findFirst().orElseThrow();
            assertEquals(INVITATION_ID, event.invitationId());
            assertEquals(ORG_ID, event.organisationId());
            assertEquals(USER_ID, event.userId());
            assertEquals(MemberRole.ADMIN, event.role());
        }
    }

    @Nested
    @DisplayName("Invitation not found")
    class InvitationNotFound {

        @Test
        @DisplayName("should throw MembershipInvitationNotFoundException")
        void shouldThrowWhenInvitationNotFound() {
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.empty());

            assertThrows(MembershipInvitationNotFoundException.class, () -> service.accept(command));
        }

        @Test
        @DisplayName("should not save anything nor publish any event")
        void shouldNotSaveOrPublishWhenNotFound() {
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.empty());

            assertThrows(MembershipInvitationNotFoundException.class, () -> service.accept(command));

            verify(invitationRepository, never()).save(any());
            verify(memberRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invitation expired")
    class InvitationExpired {

        @Test
        @DisplayName("should propagate InvitationExpiredException from the aggregate")
        void shouldPropagateInvitationExpiredException() {
            Instant past = Instant.now().minus(Duration.ofDays(10));
            MembershipInvitation expired = MembershipInvitation.reconstitute(
                    INVITATION_ID, ORG_ID, INVITED_EMAIL, MemberRole.MEMBER,
                    MembershipInvitationStatus.PENDING,
                    past, past.plus(Duration.ofDays(1)) // expired 9 days ago
            );
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(expired));

            assertThrows(InvitationExpiredException.class, () -> service.accept(command));
        }

        @Test
        @DisplayName("should not save the invitation, not create a Member, not publish")
        void shouldNotSaveCreateOrPublishWhenExpired() {
            Instant past = Instant.now().minus(Duration.ofDays(10));
            MembershipInvitation expired = MembershipInvitation.reconstitute(
                    INVITATION_ID, ORG_ID, INVITED_EMAIL, MemberRole.MEMBER,
                    MembershipInvitationStatus.PENDING,
                    past, past.plus(Duration.ofDays(1))
            );
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(expired));

            assertThrows(InvitationExpiredException.class, () -> service.accept(command));

            verify(invitationRepository, never()).save(any());
            verify(memberRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invitation not in PENDING status")
    class InvitationNotPending {

        @Test
        @DisplayName("should propagate IllegalStateException when invitation is ACCEPTED")
        void shouldPropagateWhenAlreadyAccepted() {
            Instant now = Instant.now();
            MembershipInvitation accepted = MembershipInvitation.reconstitute(
                    INVITATION_ID, ORG_ID, INVITED_EMAIL, MemberRole.MEMBER,
                    MembershipInvitationStatus.ACCEPTED,
                    now, now.plus(TTL)
            );
            when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(accepted));

            assertThrows(IllegalStateException.class, () -> service.accept(command));

            verify(invitationRepository, never()).save(any());
            verify(memberRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null invitationRepository")
        void shouldRejectNullInvitationRepository() {
            assertThrows(NullPointerException.class,
                    () -> new AcceptMembershipInvitationService(null, memberRepository, publisher));
        }

        @Test
        @DisplayName("should reject null memberRepository")
        void shouldRejectNullMemberRepository() {
            assertThrows(NullPointerException.class,
                    () -> new AcceptMembershipInvitationService(invitationRepository, null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new AcceptMembershipInvitationService(invitationRepository, memberRepository, null));
        }
    }

    @Nested
    @DisplayName("Parsing errors")
    class ParsingErrors {

        @Test
        @DisplayName("should propagate IllegalArgumentException when invitationId is not a valid UUID")
        void shouldPropagateWhenInvitationIdInvalid() {
            AcceptMembershipInvitationCommand badInvitationId =
                    new AcceptMembershipInvitationCommand("not-a-uuid", USER_ID.toString());

            assertThrows(IllegalArgumentException.class, () -> service.accept(badInvitationId));
        }

        @Test
        @DisplayName("should propagate IllegalArgumentException when userId is not a valid UUID")
        void shouldPropagateWhenUserIdInvalid() {
            AcceptMembershipInvitationCommand badUserId =
                    new AcceptMembershipInvitationCommand(INVITATION_ID.value().toString(), "not-a-uuid");

            assertThrows(IllegalArgumentException.class, () -> service.accept(badUserId));
        }
    }
}
