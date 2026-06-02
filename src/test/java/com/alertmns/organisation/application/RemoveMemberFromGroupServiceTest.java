package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.MemberRemovedFromGroup;
import com.alertmns.organisation.domain.exception.GroupMembershipNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.command.RemoveMemberFromGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RemoveMemberFromGroupService")
@ExtendWith(MockitoExtension.class)
class RemoveMemberFromGroupServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final OrganisationId OTHER_ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    GroupMembershipRepository groupMembershipRepository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    RemoveMemberFromGroupService service;

    @Nested
    @DisplayName("Removal")
    class Removal {

        GroupId groupId;
        MemberId memberId;
        GroupMembership membership;

        @BeforeEach
        void setUp() {
            groupId = GroupId.generate();
            memberId = MemberId.generate();
            lenient().when(clock.instant()).thenReturn(NOW);
            membership = GroupMembership.add(ORGANISATION_ID, groupId, memberId, NOW);
        }

        @Test
        @DisplayName("should delete the matching membership")
        void shouldDeleteTheMatchingMembership() {
            when(groupMembershipRepository.findByGroupIdAndMemberId(any(), any()))
                    .thenReturn(Optional.of(membership));

            RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            service.remove(command);

            ArgumentCaptor<GroupMembership> membershipCaptor = ArgumentCaptor.forClass(GroupMembership.class);
            verify(groupMembershipRepository).delete(membershipCaptor.capture());
            assertEquals(membership.id(), membershipCaptor.getValue().id());
        }

        @Test
        @DisplayName("should publish MemberRemovedFromGroup event with the deleted membership id")
        void shouldPublishMemberRemovedFromGroupEvent() {
            when(groupMembershipRepository.findByGroupIdAndMemberId(any(), any()))
                    .thenReturn(Optional.of(membership));

            RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            service.remove(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            MemberRemovedFromGroup event = assertInstanceOf(MemberRemovedFromGroup.class, events.getFirst());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(membership.id(), event.groupMembershipId());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("should throw GroupMembershipNotFoundException when membership not found")
        void shouldThrowGroupMembershipNotFoundExceptionWhenMembershipNotFound() {
            when(groupMembershipRepository.findByGroupIdAndMemberId(any(), any()))
                    .thenReturn(Optional.empty());

            RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            assertThrows(GroupMembershipNotFoundException.class, () -> service.remove(command));
            verify(groupMembershipRepository, never()).delete(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw OrganisationMismatchException when command organisation differs from membership")
        void shouldThrowOrganisationMismatchExceptionWhenOrganisationsDiffer() {
            GroupMembership crossOrgMembership = GroupMembership.reconstitute(
                    GroupMembershipId.generate(),
                    OTHER_ORGANISATION_ID,
                    groupId,
                    memberId,
                    NOW
            );
            when(groupMembershipRepository.findByGroupIdAndMemberId(any(), any()))
                    .thenReturn(Optional.of(crossOrgMembership));

            RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            assertThrows(OrganisationMismatchException.class, () -> service.remove(command));
            verify(groupMembershipRepository, never()).delete(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when groupId is not a valid UUID")
        void shouldThrowWhenGroupIdIsInvalid() {
            RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(
                    ORGANISATION_ID.value().toString(), "invalid", memberId.value().toString()
            );

            assertThrows(IllegalArgumentException.class, () -> service.remove(command));
            verify(groupMembershipRepository, never()).delete(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when memberId is not a valid UUID")
        void shouldThrowWhenMemberIdIsInvalid() {
            RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), "invalid"
            );

            assertThrows(IllegalArgumentException.class, () -> service.remove(command));
            verify(groupMembershipRepository, never()).delete(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null groupMembershipRepository")
        void shouldRejectNullGroupMembershipRepository() {
            assertThrows(NullPointerException.class,
                    () -> new RemoveMemberFromGroupService(null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new RemoveMemberFromGroupService(groupMembershipRepository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new RemoveMemberFromGroupService(groupMembershipRepository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command organisationId")
        void shouldRejectNullCommandOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new RemoveMemberFromGroupCommand(
                            null,
                            GroupId.generate().value().toString(),
                            MemberId.generate().value().toString()));
        }

        @Test
        @DisplayName("should reject null command groupId")
        void shouldRejectNullCommandGroupId() {
            assertThrows(NullPointerException.class,
                    () -> new RemoveMemberFromGroupCommand(
                            ORGANISATION_ID.value().toString(),
                            null,
                            MemberId.generate().value().toString()));
        }

        @Test
        @DisplayName("should reject null command memberId")
        void shouldRejectNullCommandMemberId() {
            assertThrows(NullPointerException.class,
                    () -> new RemoveMemberFromGroupCommand(
                            ORGANISATION_ID.value().toString(),
                            GroupId.generate().value().toString(),
                            null));
        }
    }
}
