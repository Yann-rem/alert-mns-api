package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.MemberSuspended;
import com.alertmns.organisation.domain.exception.LastAdminCannotBeRemovedException;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.command.SuspendMemberCommand;
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

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SuspendMemberService")
@ExtendWith(MockitoExtension.class)
class SuspendMemberServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final OrganisationId OTHER_ORGANISATION_ID = OrganisationId.generate();
    static final UUID USER_ID = UUID.randomUUID();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    MemberRepository repository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    SuspendMemberService service;

    @Nested
    @DisplayName("Suspension")
    class Suspension {

        MemberId id;
        Member activeMember;

        @BeforeEach
        void setUp() {
            id = MemberId.generate();
            lenient().when(clock.instant()).thenReturn(NOW);

            activeMember = Member.reconstitute(
                    id,
                    ORGANISATION_ID,
                    USER_ID,
                    MemberRole.MEMBER,
                    MemberStatus.ACTIVE,
                    NOW
            );
        }

        @Test
        @DisplayName("should suspend a member and save it with SUSPENDED status")
        void shouldSuspendAMemberAndSaveItWithSuspendedStatus() {
            when(repository.findById(any())).thenReturn(Optional.of(activeMember));
            SuspendMemberCommand command = new SuspendMemberCommand(
                    ORGANISATION_ID.value().toString(), id.value().toString()
            );

            service.suspend(command);

            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(repository).save(memberCaptor.capture());
            assertEquals(MemberStatus.SUSPENDED, memberCaptor.getValue().status());
        }

        @Test
        @DisplayName("should publish MemberSuspended event")
        void shouldPublishMemberSuspendedEvent() {
            when(repository.findById(any())).thenReturn(Optional.of(activeMember));
            SuspendMemberCommand command = new SuspendMemberCommand(
                    ORGANISATION_ID.value().toString(), id.value().toString()
            );

            service.suspend(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            MemberSuspended event = assertInstanceOf(MemberSuspended.class, events.getFirst());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(id, event.memberId());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("should throw MemberNotFoundException when member not found")
        void shouldThrowMemberNotFoundExceptionWhenMemberNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());
            SuspendMemberCommand command = new SuspendMemberCommand(
                    ORGANISATION_ID.value().toString(), id.value().toString()
            );
            assertThrows(MemberNotFoundException.class, () -> service.suspend(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw OrganisationMismatchException when member belongs to another organisation")
        void shouldThrowOrganisationMismatchExceptionWhenOrganisationsDiffer() {
            when(repository.findById(any())).thenReturn(Optional.of(activeMember));
            SuspendMemberCommand command = new SuspendMemberCommand(
                    OTHER_ORGANISATION_ID.value().toString(), id.value().toString()
            );

            assertThrows(OrganisationMismatchException.class, () -> service.suspend(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalStateException when member is not ACTIVE")
        void shouldThrowIllegalStateExceptionWhenMemberIsNotACTIVE() {
            Member suspendedMember = Member.reconstitute(
                    activeMember.id(),
                    activeMember.organisationId(),
                    activeMember.userId(),
                    activeMember.role(),
                    MemberStatus.SUSPENDED,
                    activeMember.joinedAt()
            );

            when(repository.findById(any())).thenReturn(Optional.of(suspendedMember));
            SuspendMemberCommand command = new SuspendMemberCommand(
                    ORGANISATION_ID.value().toString(), id.value().toString()
            );
            assertThrows(IllegalStateException.class, () -> service.suspend(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when memberId is not a valid UUID")
        void shouldThrowWhenMemberIdIsInvalid() {
            SuspendMemberCommand command = new SuspendMemberCommand(
                    ORGANISATION_ID.value().toString(), "invalid"
            );
            assertThrows(IllegalArgumentException.class, () -> service.suspend(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when organisationId is not a valid UUID")
        void shouldThrowWhenOrganisationIdIsInvalid() {
            SuspendMemberCommand command = new SuspendMemberCommand(
                    "invalid", id.value().toString()
            );
            assertThrows(IllegalArgumentException.class, () -> service.suspend(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Last-admin invariant")
    class LastAdmin {

        MemberId id;
        Member activeAdmin;

        @BeforeEach
        void setUp() {
            id = MemberId.generate();
            lenient().when(clock.instant()).thenReturn(NOW);
            activeAdmin = Member.reconstitute(
                    id, ORGANISATION_ID, USER_ID, MemberRole.ADMIN, MemberStatus.ACTIVE, NOW);
        }

        private SuspendMemberCommand command() {
            return new SuspendMemberCommand(ORGANISATION_ID.value().toString(), id.value().toString());
        }

        @Test
        @DisplayName("should refuse suspending the last active admin")
        void refusesSuspendingLastAdmin() {
            when(repository.findById(any())).thenReturn(Optional.of(activeAdmin));
            when(repository.countByOrganisationIdAndRoleAndStatus(
                    ORGANISATION_ID, MemberRole.ADMIN, MemberStatus.ACTIVE)).thenReturn(1L);

            assertThrows(LastAdminCannotBeRemovedException.class, () -> service.suspend(command()));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should allow suspending an admin when others remain")
        void allowsSuspendingWhenOthersRemain() {
            when(repository.findById(any())).thenReturn(Optional.of(activeAdmin));
            when(repository.countByOrganisationIdAndRoleAndStatus(
                    ORGANISATION_ID, MemberRole.ADMIN, MemberStatus.ACTIVE)).thenReturn(2L);

            service.suspend(command());

            verify(repository).save(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null repository")
        void shouldRejectNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new SuspendMemberService(null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new SuspendMemberService(repository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new SuspendMemberService(repository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command organisationId")
        void shouldRejectNullCommandOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new SuspendMemberCommand(null, MemberId.generate().value().toString()));
        }

        @Test
        @DisplayName("should reject null command memberId")
        void shouldRejectNullCommandMemberId() {
            assertThrows(NullPointerException.class,
                    () -> new SuspendMemberCommand(ORGANISATION_ID.value().toString(), null));
        }
    }
}
