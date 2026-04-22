package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.MemberInvited;
import com.alertmns.organisation.domain.exception.MemberAlreadyExistsException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("InviteMemberService")
@ExtendWith(MockitoExtension.class)
class InviteMemberServiceTest {

    static final String ORGANISATION_ID = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
    static final String USER_ID = "72ce2342-1d41-4e0b-b4b8-6e88891e9add";

    @Mock
    MemberRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    InviteMemberService service;

    @Nested
    @DisplayName("Invitation")
    class Invitation {

        @Test
        @DisplayName("should save the member with organisationId, userId, role MEMBER and status PENDING")
        void shouldSaveTheMemberWithMemberRole() {
            when(repository.existsByOrganisationIdAndUserId(any(), any())).thenReturn(false);

            InviteMemberCommand command = new InviteMemberCommand(ORGANISATION_ID, USER_ID, "MEMBER");

            service.invite(command);

            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(repository).save(memberCaptor.capture());
            Member saved = memberCaptor.getValue();
            assertEquals(OrganisationId.from(ORGANISATION_ID), saved.organisationId());
            assertEquals(UUID.fromString(USER_ID), saved.userId());
            assertEquals(MemberRole.MEMBER, saved.role());
            assertEquals(MemberStatus.PENDING, saved.status());
        }

        @Test
        @DisplayName("should save the member with role ADMIN when command role is ADMIN")
        void shouldSaveTheMemberWithAdminRole() {
            when(repository.existsByOrganisationIdAndUserId(any(), any())).thenReturn(false);

            InviteMemberCommand command = new InviteMemberCommand(ORGANISATION_ID, USER_ID, "ADMIN");

            service.invite(command);

            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(repository).save(memberCaptor.capture());
            assertEquals(MemberRole.ADMIN, memberCaptor.getValue().role());
        }

        @Test
        @DisplayName("should publish MemberInvited event with the saved member id")
        void shouldPublishMemberInvitedEvent() {
            when(repository.existsByOrganisationIdAndUserId(any(), any())).thenReturn(false);

            InviteMemberCommand command = new InviteMemberCommand(ORGANISATION_ID, USER_ID, "MEMBER");

            service.invite(command);

            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(repository).save(memberCaptor.capture());
            Member saved = memberCaptor.getValue();

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            MemberInvited event = assertInstanceOf(MemberInvited.class, events.getFirst());
            assertEquals(saved.id(), event.memberId());
        }

        @Test
        @DisplayName("should throw MemberAlreadyExistsException when user is already a member of the organisation")
        void shouldThrowMemberAlreadyExistsExceptionWhenUserIsAlreadyAMember() {
            when(repository.existsByOrganisationIdAndUserId(any(), any())).thenReturn(true);

            InviteMemberCommand command = new InviteMemberCommand(ORGANISATION_ID, USER_ID, "MEMBER");

            assertThrows(MemberAlreadyExistsException.class, () -> service.invite(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when organisationId is not a valid UUID")
        void shouldThrowWhenOrganisationIdIsInvalid() {
            InviteMemberCommand command = new InviteMemberCommand("invalid", USER_ID, "MEMBER");

            assertThrows(IllegalArgumentException.class, () -> service.invite(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when userId is not a valid UUID")
        void shouldThrowWhenUserIdIsInvalid() {
            InviteMemberCommand command = new InviteMemberCommand(ORGANISATION_ID, "invalid", "MEMBER");

            assertThrows(IllegalArgumentException.class, () -> service.invite(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when role is not a valid MemberRole")
        void shouldThrowWhenRoleIsInvalid() {
            InviteMemberCommand command = new InviteMemberCommand(ORGANISATION_ID, USER_ID, "INVALID");

            assertThrows(IllegalArgumentException.class, () -> service.invite(command));
            verify(repository, never()).existsByOrganisationIdAndUserId(any(), any());
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
                    () -> new InviteMemberService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new InviteMemberService(repository, null));
        }

        @Test
        @DisplayName("should reject null command organisationId")
        void shouldRejectNullCommandOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new InviteMemberCommand(null, USER_ID, "MEMBER"));
        }

        @Test
        @DisplayName("should reject null command userId")
        void shouldRejectNullCommandUserId() {
            assertThrows(NullPointerException.class,
                    () -> new InviteMemberCommand(ORGANISATION_ID, null, "MEMBER"));
        }

        @Test
        @DisplayName("should reject null command role")
        void shouldRejectNullCommandRole() {
            assertThrows(NullPointerException.class,
                    () -> new InviteMemberCommand(ORGANISATION_ID, USER_ID, null));
        }
    }
}
