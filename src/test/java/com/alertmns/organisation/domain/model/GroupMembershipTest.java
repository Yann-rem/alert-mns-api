package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.MemberAddedToGroup;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GroupMembership")
class GroupMembershipTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final MemberId MEMBER_ID = MemberId.generate();
    static final GroupId GROUP_ID = GroupId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should add a member to a group with joinedAt = now")
        void shouldAddAMemberToAGroup() {
            GroupMembership membership = GroupMembership.add(ORGANISATION_ID, GROUP_ID, MEMBER_ID, NOW);
            assertEquals(MEMBER_ID, membership.memberId());
            assertEquals(GROUP_ID, membership.groupId());
            assertNotNull(membership.id());
            assertEquals(NOW, membership.joinedAt());
        }

        @Test
        @DisplayName("should reconstitute an existing group membership")
        void shouldReconstituteAnExistingGroupMembership() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();

            GroupMembership membership = GroupMembership.reconstitute(
                    id, ORGANISATION_ID, GROUP_ID, MEMBER_ID, joinedAt
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
        @DisplayName("should reject null organisationId")
        void shouldRejectNullOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.add(null, GROUP_ID, MEMBER_ID, NOW));
        }

        @Test
        @DisplayName("should reject null groupId")
        void shouldRejectNullGroupId() {
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.add(ORGANISATION_ID, null, MEMBER_ID, NOW));
        }

        @Test
        @DisplayName("should reject null memberId")
        void shouldRejectNullMemberId() {
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.add(ORGANISATION_ID, GROUP_ID, null, NOW));
        }

        @Test
        @DisplayName("should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            Instant joinedAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.reconstitute(null, ORGANISATION_ID, GROUP_ID, MEMBER_ID, joinedAt));
        }

        @Test
        @DisplayName("should reject null organisationId on reconstitute")
        void shouldRejectNullOrganisationIdOnReconstitute() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.reconstitute(id, null, GROUP_ID, MEMBER_ID, joinedAt));
        }

        @Test
        @DisplayName("should reject null groupId on reconstitute")
        void shouldRejectNullGroupIdOnReconstitute() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.reconstitute(id, ORGANISATION_ID, null, MEMBER_ID, joinedAt));
        }

        @Test
        @DisplayName("should reject null memberId on reconstitute")
        void shouldRejectNullMemberIdOnReconstitute() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.reconstitute(id, ORGANISATION_ID, GROUP_ID, null, joinedAt));
        }

        @Test
        @DisplayName("should reject null joinedAt on reconstitute")
        void shouldRejectNullJoinedAtOnReconstitute() {
            GroupMembershipId id = GroupMembershipId.generate();
            assertThrows(NullPointerException.class,
                    () -> GroupMembership.reconstitute(id, ORGANISATION_ID, GROUP_ID, MEMBER_ID, null));
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        @Test
        @DisplayName("add should emit MemberAddedToGroup with occurredOn = now")
        void addShouldEmitMemberAddedToGroup() {
            GroupMembership membership = GroupMembership.add(ORGANISATION_ID, GROUP_ID, MEMBER_ID, NOW);

            List<DomainEvent> events = membership.pullDomainEvents();
            assertEquals(1, events.size());
            MemberAddedToGroup event = assertInstanceOf(MemberAddedToGroup.class, events.getFirst());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(membership.id(), event.groupMembershipId());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("reconstitute should not emit any event")
        void reconstituteShouldNotEmitAnyEvent() {
            GroupMembershipId id = GroupMembershipId.generate();
            Instant joinedAt = Instant.now();

            GroupMembership membership = GroupMembership.reconstitute(
                    id, ORGANISATION_ID, GROUP_ID, MEMBER_ID, joinedAt
            );

            List<DomainEvent> events = membership.pullDomainEvents();
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("pullDomainEvents should clear events after pull")
        void pullDomainEventsShouldClearEventsAfterPull() {
            GroupMembership membership = GroupMembership.add(ORGANISATION_ID, GROUP_ID, MEMBER_ID, NOW);
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
                    id, ORGANISATION_ID, GROUP_ID, MEMBER_ID, joinedAt
            );

            GroupMembership membership2 = GroupMembership.reconstitute(
                    id, ORGANISATION_ID, GROUP_ID, MEMBER_ID, joinedAt
            );

            assertEquals(membership1, membership2);
        }

        @Test
        @DisplayName("two group memberships with different ids should not be equal")
        void twoGroupMembershipsWithDifferentIdsShouldNotBeEqual() {
            GroupMembership membership1 = GroupMembership.add(ORGANISATION_ID, GROUP_ID, MEMBER_ID, NOW);
            GroupMembership membership2 = GroupMembership.add(ORGANISATION_ID, GROUP_ID, MEMBER_ID, NOW);
            assertNotEquals(membership1, membership2);
        }
    }
}
