package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.MemberAddedToGroup;
import com.alertmns.organisation.domain.exception.GroupMembershipAlreadyExistsException;
import com.alertmns.organisation.domain.exception.GroupNotFoundException;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.command.AddMemberToGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
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

@DisplayName("AddMemberToGroupService")
@ExtendWith(MockitoExtension.class)
class AddMemberToGroupServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final OrganisationId OTHER_ORGANISATION_ID = OrganisationId.generate();

    @Mock
    GroupRepository groupRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    GroupMembershipRepository groupMembershipRepository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    AddMemberToGroupService service;

    @Nested
    @DisplayName("Addition")
    class Addition {

        GroupId groupId;
        MemberId memberId;
        Group group;
        Member member;

        @BeforeEach
        void setUp() {
            groupId = GroupId.generate();
            memberId = MemberId.generate();

            group = Group.reconstitute(
                    groupId,
                    ORGANISATION_ID,
                    GroupName.of("Développeurs"),
                    Instant.now()
            );

            member = Member.reconstitute(
                    memberId,
                    ORGANISATION_ID,
                    UUID.randomUUID(),
                    MemberRole.MEMBER,
                    MemberStatus.ACTIVE,
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should add a member to a group and save the membership")
        void shouldAddAMemberToAGroupAndSaveTheMembership() {
            when(groupRepository.findById(any())).thenReturn(Optional.of(group));
            when(memberRepository.findById(any())).thenReturn(Optional.of(member));
            when(groupMembershipRepository.existsByGroupIdAndMemberId(any(), any())).thenReturn(false);

            AddMemberToGroupCommand command = new AddMemberToGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            service.add(command);

            ArgumentCaptor<GroupMembership> membershipCaptor = ArgumentCaptor.forClass(GroupMembership.class);
            verify(groupMembershipRepository).save(membershipCaptor.capture());
            GroupMembership saved = membershipCaptor.getValue();
            assertEquals(groupId, saved.groupId());
            assertEquals(memberId, saved.memberId());
        }

        @Test
        @DisplayName("should publish MemberAddedToGroup event")
        void shouldPublishMemberAddedToGroupEvent() {
            when(groupRepository.findById(any())).thenReturn(Optional.of(group));
            when(memberRepository.findById(any())).thenReturn(Optional.of(member));
            when(groupMembershipRepository.existsByGroupIdAndMemberId(any(), any())).thenReturn(false);

            AddMemberToGroupCommand command = new AddMemberToGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            service.add(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            MemberAddedToGroup event = assertInstanceOf(MemberAddedToGroup.class, events.getFirst());
            assertEquals(ORGANISATION_ID, event.organisationId());
        }

        @Test
        @DisplayName("should throw GroupNotFoundException when group not found")
        void shouldThrowGroupNotFoundExceptionWhenGroupNotFound() {
            when(groupRepository.findById(any())).thenReturn(Optional.empty());

            AddMemberToGroupCommand command = new AddMemberToGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            assertThrows(GroupNotFoundException.class, () -> service.add(command));
            verify(groupMembershipRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw MemberNotFoundException when member not found")
        void shouldThrowMemberNotFoundExceptionWhenMemberNotFound() {
            when(groupRepository.findById(any())).thenReturn(Optional.of(group));
            when(memberRepository.findById(any())).thenReturn(Optional.empty());

            AddMemberToGroupCommand command = new AddMemberToGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            assertThrows(MemberNotFoundException.class, () -> service.add(command));
            verify(groupMembershipRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw OrganisationMismatchException when member and group belong to different organisations")
        void shouldThrowOrganisationMismatchExceptionWhenOrganisationsDiffer() {
            Member crossOrgMember = Member.reconstitute(
                    memberId,
                    OTHER_ORGANISATION_ID,
                    UUID.randomUUID(),
                    MemberRole.MEMBER,
                    MemberStatus.ACTIVE,
                    Instant.now()
            );

            when(groupRepository.findById(any())).thenReturn(Optional.of(group));
            when(memberRepository.findById(any())).thenReturn(Optional.of(crossOrgMember));

            AddMemberToGroupCommand command = new AddMemberToGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            assertThrows(OrganisationMismatchException.class, () -> service.add(command));
            verify(groupMembershipRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw GroupMembershipAlreadyExistsException when member is already in the group")
        void shouldThrowGroupMembershipAlreadyExistsExceptionWhenMembershipAlreadyExists() {
            when(groupRepository.findById(any())).thenReturn(Optional.of(group));
            when(memberRepository.findById(any())).thenReturn(Optional.of(member));
            when(groupMembershipRepository.existsByGroupIdAndMemberId(any(), any())).thenReturn(true);

            AddMemberToGroupCommand command = new AddMemberToGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), memberId.value().toString()
            );

            assertThrows(GroupMembershipAlreadyExistsException.class, () -> service.add(command));
            verify(groupMembershipRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when groupId is not a valid UUID")
        void shouldThrowWhenGroupIdIsInvalid() {
            AddMemberToGroupCommand command = new AddMemberToGroupCommand(
                    ORGANISATION_ID.value().toString(), "invalid", memberId.value().toString()
            );

            assertThrows(IllegalArgumentException.class, () -> service.add(command));
            verify(groupMembershipRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when memberId is not a valid UUID")
        void shouldThrowWhenMemberIdIsInvalid() {
            AddMemberToGroupCommand command = new AddMemberToGroupCommand(
                    ORGANISATION_ID.value().toString(), groupId.value().toString(), "invalid"
            );

            assertThrows(IllegalArgumentException.class, () -> service.add(command));
            verify(groupMembershipRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null groupRepository")
        void shouldRejectNullGroupRepository() {
            assertThrows(NullPointerException.class,
                    () -> new AddMemberToGroupService(null, memberRepository, groupMembershipRepository, publisher));
        }

        @Test
        @DisplayName("should reject null memberRepository")
        void shouldRejectNullMemberRepository() {
            assertThrows(NullPointerException.class,
                    () -> new AddMemberToGroupService(groupRepository, null, groupMembershipRepository, publisher));
        }

        @Test
        @DisplayName("should reject null groupMembershipRepository")
        void shouldRejectNullGroupMembershipRepository() {
            assertThrows(NullPointerException.class,
                    () -> new AddMemberToGroupService(groupRepository, memberRepository, null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new AddMemberToGroupService(groupRepository, memberRepository, groupMembershipRepository, null));
        }

        @Test
        @DisplayName("should reject null command organisationId")
        void shouldRejectNullCommandOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new AddMemberToGroupCommand(
                            null,
                            GroupId.generate().value().toString(),
                            MemberId.generate().value().toString()));
        }

        @Test
        @DisplayName("should reject null command groupId")
        void shouldRejectNullCommandGroupId() {
            assertThrows(NullPointerException.class,
                    () -> new AddMemberToGroupCommand(
                            ORGANISATION_ID.value().toString(),
                            null,
                            MemberId.generate().value().toString()));
        }

        @Test
        @DisplayName("should reject null command memberId")
        void shouldRejectNullCommandMemberId() {
            assertThrows(NullPointerException.class,
                    () -> new AddMemberToGroupCommand(
                            ORGANISATION_ID.value().toString(),
                            GroupId.generate().value().toString(),
                            null));
        }
    }
}
