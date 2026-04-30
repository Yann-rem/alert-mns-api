package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.MemberActivated;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.command.ActivateMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.EventPublisher;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ActivateMemberService")
@ExtendWith(MockitoExtension.class)
class ActivateMemberServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final OrganisationId OTHER_ORGANISATION_ID = OrganisationId.generate();
    static final UUID USER_ID = UUID.randomUUID();

    @Mock
    MemberRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    ActivateMemberService service;

    @Nested
    @DisplayName("Activation")
    class Activation {

        MemberId id;
        Member pendingMember;

        @BeforeEach
        void setUp() {
            id = MemberId.generate();

            pendingMember = Member.reconstitute(
                    id,
                    ORGANISATION_ID,
                    USER_ID,
                    MemberRole.MEMBER,
                    MemberStatus.PENDING,
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should save the member with status ACTIVE")
        void shouldSaveTheMemberWithStatusActive() {
            when(repository.findById(any())).thenReturn(Optional.of(pendingMember));
            ActivateMemberCommand command = new ActivateMemberCommand(
                    ORGANISATION_ID.value().toString(), id.value().toString()
            );

            service.activate(command);

            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(repository).save(memberCaptor.capture());
            Member saved = memberCaptor.getValue();
            assertEquals(id, saved.id());
            assertEquals(MemberStatus.ACTIVE, saved.status());
        }

        @Test
        @DisplayName("should publish MemberActivated event with the activated member id")
        void shouldPublishMemberActivatedEvent() {
            when(repository.findById(any())).thenReturn(Optional.of(pendingMember));
            ActivateMemberCommand command = new ActivateMemberCommand(
                    ORGANISATION_ID.value().toString(), id.value().toString()
            );

            service.activate(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            MemberActivated event = assertInstanceOf(MemberActivated.class, events.getFirst());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(id, event.memberId());
        }

        @Test
        @DisplayName("should throw MemberNotFoundException when member not found")
        void shouldThrowMemberNotFoundExceptionWhenMemberNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());
            ActivateMemberCommand command = new ActivateMemberCommand(
                    ORGANISATION_ID.value().toString(), id.value().toString()
            );
            assertThrows(MemberNotFoundException.class, () -> service.activate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw OrganisationMismatchException when member belongs to another organisation")
        void shouldThrowOrganisationMismatchExceptionWhenOrganisationsDiffer() {
            when(repository.findById(any())).thenReturn(Optional.of(pendingMember));
            ActivateMemberCommand command = new ActivateMemberCommand(
                    OTHER_ORGANISATION_ID.value().toString(), id.value().toString()
            );

            assertThrows(OrganisationMismatchException.class, () -> service.activate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalStateException when member is not PENDING")
        void shouldThrowIllegalStateExceptionWhenMemberIsNotPENDING() {
            Member activeMember = Member.reconstitute(
                    pendingMember.id(),
                    pendingMember.organisationId(),
                    pendingMember.userId(),
                    pendingMember.role(),
                    MemberStatus.ACTIVE,
                    pendingMember.joinedAt()
            );

            when(repository.findById(any())).thenReturn(Optional.of(activeMember));
            ActivateMemberCommand command = new ActivateMemberCommand(
                    ORGANISATION_ID.value().toString(), id.value().toString()
            );
            assertThrows(IllegalStateException.class, () -> service.activate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when memberId is not a valid UUID")
        void shouldThrowWhenMemberIdIsInvalid() {
            ActivateMemberCommand command = new ActivateMemberCommand(
                    ORGANISATION_ID.value().toString(), "invalid"
            );
            assertThrows(IllegalArgumentException.class, () -> service.activate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when organisationId is not a valid UUID")
        void shouldThrowWhenOrganisationIdIsInvalid() {
            ActivateMemberCommand command = new ActivateMemberCommand(
                    "invalid", id.value().toString()
            );
            assertThrows(IllegalArgumentException.class, () -> service.activate(command));
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
                    () -> new ActivateMemberService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new ActivateMemberService(repository, null));
        }

        @Test
        @DisplayName("should reject null command organisationId")
        void shouldRejectNullCommandOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new ActivateMemberCommand(null, MemberId.generate().value().toString()));
        }

        @Test
        @DisplayName("should reject null command memberId")
        void shouldRejectNullCommandMemberId() {
            assertThrows(NullPointerException.class,
                    () -> new ActivateMemberCommand(ORGANISATION_ID.value().toString(), null));
        }
    }
}
