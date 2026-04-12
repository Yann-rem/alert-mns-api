package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.MemberActivated;
import com.alertmns.organisation.domain.event.MemberInvited;
import com.alertmns.organisation.domain.event.MemberReactivated;
import com.alertmns.organisation.domain.event.MemberSuspended;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Member")
class MemberTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final UUID USER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should invite a new member with PENDING status")
        void shouldInviteANewMemberWithPENDINGStatus() {
            Member member = Member.invite(ORGANISATION_ID, USER_ID, MemberRole.MEMBER);
            assertEquals(MemberStatus.PENDING, member.status());
            assertEquals(MemberRole.MEMBER, member.role());
            assertEquals(ORGANISATION_ID, member.organisationId());
            assertEquals(USER_ID, member.userId());
            assertNotNull(member.id());
            assertNotNull(member.joinedAt());
        }

        @Test
        @DisplayName("should invite a new member with ADMIN role")
        void shouldInviteANewMemberWithADMINRole() {
            Member member = Member.invite(ORGANISATION_ID, USER_ID, MemberRole.ADMIN);
            assertEquals(MemberRole.ADMIN, member.role());
            assertEquals(MemberStatus.PENDING, member.status());
        }

        @Test
        @DisplayName("should reconstitute an existing member")
        void shouldReconstituteAnExistingMember() {
            MemberId id = MemberId.generate();
            Instant joinedAt = Instant.now();

            Member member = Member.reconstitute(
                    id, ORGANISATION_ID, USER_ID,
                    MemberRole.ADMIN, MemberStatus.ACTIVE, joinedAt
            );

            assertEquals(id, member.id());
            assertEquals(ORGANISATION_ID, member.organisationId());
            assertEquals(USER_ID, member.userId());
            assertEquals(MemberRole.ADMIN, member.role());
            assertEquals(MemberStatus.ACTIVE, member.status());
            assertEquals(joinedAt, member.joinedAt());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null organisationId")
        void shouldRejectNullOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> Member.invite(null, USER_ID, MemberRole.MEMBER));
        }

        @Test
        @DisplayName("should reject null userId")
        void shouldRejectNullUserId() {
            assertThrows(NullPointerException.class,
                    () -> Member.invite(ORGANISATION_ID, null, MemberRole.MEMBER));
        }

        @Test
        @DisplayName("should reject null role")
        void shouldRejectNullRole() {
            assertThrows(NullPointerException.class,
                    () -> Member.invite(ORGANISATION_ID, USER_ID, null));
        }
    }

    @Nested
    @DisplayName("Behaviour")
    class Behaviour {

        Member member;

        @BeforeEach
        void setUp() {
            member = Member.invite(ORGANISATION_ID, USER_ID, MemberRole.MEMBER);
        }

        @Test
        @DisplayName("activate should transition PENDING to ACTIVE")
        void activateShouldTransitionPENDINGToACTIVE() {
            member.activate();
            assertEquals(MemberStatus.ACTIVE, member.status());
        }

        @Test
        @DisplayName("activate should reject non-PENDING member")
        void activateShouldRejectNonPENDINGMember() {
            member.activate();
            assertThrows(IllegalStateException.class, () -> member.activate());
        }

        @Test
        @DisplayName("suspend should transition ACTIVE to SUSPENDED")
        void suspendShouldTransitionACTIVEToSUSPENDED() {
            member.activate();
            member.suspend();
            assertEquals(MemberStatus.SUSPENDED, member.status());
        }

        @Test
        @DisplayName("suspend should reject non-ACTIVE member")
        void suspendShouldRejectNonACTIVEMember() {
            assertThrows(IllegalStateException.class, () -> member.suspend());
        }

        @Test
        @DisplayName("reactivate should transition SUSPENDED to ACTIVE")
        void reactivateShouldTransitionSUSPENDEDToACTIVE() {
            member.activate();
            member.suspend();
            member.reactivate();
            assertEquals(MemberStatus.ACTIVE, member.status());
        }

        @Test
        @DisplayName("reactivate should reject non-SUSPENDED member")
        void reactivateShouldRejectNonSUSPENDEDMember() {
            assertThrows(IllegalStateException.class, () -> member.reactivate());
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        Member member;

        @BeforeEach
        void setUp() {
            member = Member.invite(ORGANISATION_ID, USER_ID, MemberRole.MEMBER);
        }

        @Test
        @DisplayName("invite should emit MemberInvited")
        void inviteShouldEmitMemberInvited() {
            List<DomainEvent> events = member.pullDomainEvents();
            assertEquals(1, events.size());
            MemberInvited event = assertInstanceOf(MemberInvited.class, events.getFirst());
            assertEquals(member.id(), event.memberId());
        }

        @Test
        @DisplayName("activate should emit MemberActivated")
        void activateShouldEmitMemberActivated() {
            member.pullDomainEvents();
            member.activate();
            List<DomainEvent> events = member.pullDomainEvents();
            assertEquals(1, events.size());
            MemberActivated event = assertInstanceOf(MemberActivated.class, events.getFirst());
            assertEquals(member.id(), event.memberId());
        }

        @Test
        @DisplayName("suspend should emit MemberSuspended")
        void suspendShouldEmitMemberSuspended() {
            member.pullDomainEvents();
            member.activate();
            member.pullDomainEvents();
            member.suspend();
            List<DomainEvent> events = member.pullDomainEvents();
            assertEquals(1, events.size());
            MemberSuspended event = assertInstanceOf(MemberSuspended.class, events.getFirst());
            assertEquals(member.id(), event.memberId());
        }

        @Test
        @DisplayName("reactivate should emit MemberReactivated")
        void reactivateShouldEmitMemberReactivated() {
            member.pullDomainEvents();
            member.activate();
            member.pullDomainEvents();
            member.suspend();
            member.pullDomainEvents();
            member.reactivate();
            List<DomainEvent> events = member.pullDomainEvents();
            assertEquals(1, events.size());
            MemberReactivated event = assertInstanceOf(MemberReactivated.class, events.getFirst());
            assertEquals(member.id(), event.memberId());
        }

        @Test
        @DisplayName("reconstitute should not emit any event")
        void reconstituteShouldNotEmitAnyEvent() {
            MemberId id = MemberId.generate();
            Instant joinedAt = Instant.now();

            Member reconstituted = Member.reconstitute(
                    id, ORGANISATION_ID, USER_ID,
                    MemberRole.MEMBER, MemberStatus.ACTIVE, joinedAt
            );

            List<DomainEvent> events = reconstituted.pullDomainEvents();
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("pullDomainEvents should clear events after pull")
        void pullDomainEventsShouldClearEventsAfterPull() {
            member.pullDomainEvents();
            List<DomainEvent> events = member.pullDomainEvents();
            assertTrue(events.isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two members with same id should be equal")
        void twoMembersWithSameIdShouldBeEqual() {
            MemberId id = MemberId.generate();
            Instant joinedAt = Instant.now();

            Member member1 = Member.reconstitute(
                    id, ORGANISATION_ID, USER_ID,
                    MemberRole.MEMBER, MemberStatus.ACTIVE, joinedAt
            );

            Member member2 = Member.reconstitute(
                    id, ORGANISATION_ID, USER_ID,
                    MemberRole.MEMBER, MemberStatus.ACTIVE, joinedAt
            );

            assertEquals(member1, member2);
        }

        @Test
        @DisplayName("two members with different ids should not be equal")
        void twoMembersWithDifferentIdsShouldNotBeEqual() {
            Member member1 = Member.invite(ORGANISATION_ID, USER_ID, MemberRole.MEMBER);
            Member member2 = Member.invite(ORGANISATION_ID, USER_ID, MemberRole.MEMBER);
            assertNotEquals(member1, member2);
        }
    }
}
