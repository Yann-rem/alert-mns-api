package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.MemberAddedToGroup;
import com.alertmns.shared.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GroupMembership")
class GroupMembershipTest {

    static final MemberId MEMBER_ID = MemberId.generate();
    static final GroupId GROUP_ID = GroupId.generate();

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should add a member to a group")
        void shouldAddAMemberToAGroup() {
            GroupMembership membership = GroupMembership.add(GROUP_ID, MEMBER_ID);
            assertEquals(MEMBER_ID, membership.memberId());
            assertEquals(GROUP_ID, membership.groupId());
            assertNotNull(membership.id());
            assertNotNull(membership.joinedAt());
        }

        @Test
        @DisplayName("should reconstitute an existing group membership")
        void shouldReconstituteAnExistingGroupMembership() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();

            GroupMembership membership = GroupMembership.reconstitute(
                    id, GROUP_ID, MEMBER_ID, joinedAt
            );

            assertEquals(id, membership.id());
            assertEquals(MEMBER_ID, membership.memberId());
            assertEquals(GROUP_ID, membership.groupId());
            assertEquals(joinedAt, membership.joinedAt());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null groupId")
        void shouldRejectNullGroupId() {
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.add(null, MEMBER_ID));
        }

        @Test
        @DisplayName("should reject null memberId")
        void shouldRejectNullMemberId() {
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.add(GROUP_ID, null));
        }

        @Test
        @DisplayName("should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            Instant joinedAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.reconstitute(null, GROUP_ID, MEMBER_ID, joinedAt));
        }

        @Test
        @DisplayName("should reject null groupId on reconstitute")
        void shouldRejectNullGroupIdOnReconstitute() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.reconstitute(id, null, MEMBER_ID, joinedAt));
        }

        @Test
        @DisplayName("should reject null memberId on reconstitute")
        void shouldRejectNullMemberIdOnReconstitute() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.reconstitute(id, GROUP_ID, null, joinedAt));
        }

        @Test
        @DisplayName("should reject null joinedAt on reconstitute")
        void shouldRejectNullJoinedAtOnReconstitute() {
            GroupMembershipId id = GroupMembershipId.generate();
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.reconstitute(id, GROUP_ID, MEMBER_ID, null));
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        @Test
        @DisplayName("add should emit MemberAddedToGroup")
        void addShouldEmitMemberAddedToGroup() {
            GroupMembership membership = GroupMembership.add(GROUP_ID, MEMBER_ID);

            List<DomainEvent> events = membership.pullDomainEvents();
            assertEquals(1, events.size());
            MemberAddedToGroup event = assertInstanceOf(MemberAddedToGroup.class, events.getFirst());
            assertEquals(membership.id(), event.groupMembershipId());
        }

        @Test
        @DisplayName("reconstitute should not emit any event")
        void reconstituteShouldNotEmitAnyEvent() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();

            GroupMembership membership = GroupMembership.reconstitute(
                    id, GROUP_ID, MEMBER_ID, joinedAt
            );

            List<DomainEvent> events = membership.pullDomainEvents();
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("pullDomainEvents should clear events after pull")
        void pullDomainEventsShouldClearEventsAfterPull() {
            GroupMembership membership = GroupMembership.add(GROUP_ID, MEMBER_ID);
            membership.pullDomainEvents();
            List<DomainEvent> events = membership.pullDomainEvents();
            assertTrue(events.isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two group memberships with same id should be equal")
        void twoGroupMembershipsWithSameIdShouldBeEqual() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();

            GroupMembership membership1 = GroupMembership.reconstitute(
                    id, GROUP_ID, MEMBER_ID, joinedAt
            );

            GroupMembership membership2 = GroupMembership.reconstitute(
                    id, GROUP_ID, MEMBER_ID, joinedAt
            );

            assertEquals(membership1, membership2);
        }

        @Test
        @DisplayName("two group memberships with different ids should not be equal")
        void twoGroupMembershipsWithDifferentIdsShouldNotBeEqual() {
            GroupMembership membership1 = GroupMembership.add(GROUP_ID, MEMBER_ID);
            GroupMembership membership2 = GroupMembership.add(GROUP_ID, MEMBER_ID);
            assertNotEquals(membership1, membership2);
        }
    }
}
