package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.MembershipInvitationAccepted;
import com.alertmns.organisation.domain.event.MembershipInvitationIssued;
import com.alertmns.organisation.domain.exception.InvitationExpiredException;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.Email;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MembershipInvitation")
class MembershipInvitationTest {

    static final OrganisationId ORG_ID = OrganisationId.generate();
    static final Email EMAIL = Email.of("invited@example.com");
    static final MemberRole ROLE = MemberRole.MEMBER;
    static final Duration TTL = Duration.ofDays(7);
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Nested
    @DisplayName("Issuance")
    class Issuance {

        @Test
        @DisplayName("should create an invitation in PENDING status with createdAt = now")
        void shouldCreateAnInvitationInPendingStatus() {
            MembershipInvitation invitation = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, NOW, TTL);

            assertEquals(MembershipInvitationStatus.PENDING, invitation.status());
            assertEquals(ORG_ID, invitation.organisationId());
            assertEquals(EMAIL, invitation.invitedEmail());
            assertEquals(ROLE, invitation.role());
            assertNotNull(invitation.id());
            assertEquals(NOW, invitation.createdAt());
        }

        @Test
        @DisplayName("should set expiresAt = now + ttl")
        void shouldSetExpiresAtBasedOnTtl() {
            MembershipInvitation invitation = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, NOW, TTL);

            assertEquals(NOW.plus(TTL), invitation.expiresAt());
        }

        @Test
        @DisplayName("should emit MembershipInvitationIssued with occurredOn = now")
        void shouldEmitMembershipInvitationIssued() {
            MembershipInvitation invitation = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, NOW, TTL);

            List<DomainEvent> events = invitation.pullDomainEvents();
            assertEquals(1, events.size());
            MembershipInvitationIssued event = assertInstanceOf(
                    MembershipInvitationIssued.class, events.getFirst());
            assertEquals(invitation.id(), event.invitationId());
            assertEquals(ORG_ID, event.organisationId());
            assertEquals(EMAIL, event.invitedEmail());
            assertEquals(ROLE, event.role());
            assertEquals(NOW, event.occurredOn());
        }
    }

    @Nested
    @DisplayName("Reconstitution")
    class Reconstitution {

        @Test
        @DisplayName("should reconstitute without emitting any event")
        void shouldReconstituteWithoutEmittingAnyEvent() {
            MembershipInvitationId id = MembershipInvitationId.generate();
            Instant createdAt = NOW;
            Instant expiresAt = createdAt.plus(TTL);

            MembershipInvitation invitation = MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE,
                    MembershipInvitationStatus.PENDING,
                    createdAt, expiresAt
            );

            assertEquals(id, invitation.id());
            assertEquals(MembershipInvitationStatus.PENDING, invitation.status());
            assertTrue(invitation.pullDomainEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("Accept")
    class Accept {

        MembershipInvitation pendingInvitation;
        UUID userId;

        @BeforeEach
        void setUp() {
            pendingInvitation = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, NOW, TTL);
            pendingInvitation.pullDomainEvents(); // clear MembershipInvitationIssued
            userId = UUID.randomUUID();
        }

        @Test
        @DisplayName("should transition PENDING to ACCEPTED")
        void shouldTransitionPendingToAccepted() {
            pendingInvitation.accept(NOW, userId);

            assertEquals(MembershipInvitationStatus.ACCEPTED, pendingInvitation.status());
        }

        @Test
        @DisplayName("should emit MembershipInvitationAccepted with userId, role and occurredOn")
        void shouldEmitMembershipInvitationAccepted() {
            Instant acceptedAt = NOW.plusSeconds(120);
            pendingInvitation.accept(acceptedAt, userId);

            List<DomainEvent> events = pendingInvitation.pullDomainEvents();
            assertEquals(1, events.size());
            MembershipInvitationAccepted event = assertInstanceOf(
                    MembershipInvitationAccepted.class, events.getFirst());
            assertEquals(pendingInvitation.id(), event.invitationId());
            assertEquals(ORG_ID, event.organisationId());
            assertEquals(userId, event.userId());
            assertEquals(ROLE, event.role());
            assertEquals(acceptedAt, event.occurredOn());
        }

        @Test
        @DisplayName("should throw InvitationExpiredException when now > expiresAt")
        void shouldThrowWhenExpired() {
            Instant afterExpiry = pendingInvitation.expiresAt().plusSeconds(1);

            assertThrows(InvitationExpiredException.class,
                    () -> pendingInvitation.accept(afterExpiry, userId));
            assertEquals(MembershipInvitationStatus.PENDING, pendingInvitation.status(),
                    "Status must not mutate when acceptance is rejected");
            assertTrue(pendingInvitation.pullDomainEvents().isEmpty(),
                    "No event must be emitted when acceptance is rejected");
        }

        @Test
        @DisplayName("should accept exactly at expiresAt (boundary inclusive)")
        void shouldAcceptExactlyAtExpiresAt() {
            // Politique : l'invitation est valide tant que now <= expiresAt.
            pendingInvitation.accept(pendingInvitation.expiresAt(), userId);

            assertEquals(MembershipInvitationStatus.ACCEPTED, pendingInvitation.status());
        }

        @Test
        @DisplayName("should throw IllegalStateException when already ACCEPTED")
        void shouldThrowWhenAlreadyAccepted() {
            pendingInvitation.accept(NOW, userId);
            pendingInvitation.pullDomainEvents();

            assertThrows(IllegalStateException.class,
                    () -> pendingInvitation.accept(NOW, UUID.randomUUID()));
        }

        @Test
        @DisplayName("should throw IllegalStateException when status is EXPIRED")
        void shouldThrowWhenStatusIsExpired() {
            MembershipInvitation expired = MembershipInvitation.reconstitute(
                    MembershipInvitationId.generate(), ORG_ID, EMAIL, ROLE,
                    MembershipInvitationStatus.EXPIRED,
                    NOW.minus(Duration.ofDays(10)),
                    NOW.minus(Duration.ofDays(3))
            );

            assertThrows(IllegalStateException.class,
                    () -> expired.accept(NOW, userId));
        }

        @Test
        @DisplayName("should throw IllegalStateException when status is REVOKED")
        void shouldThrowWhenStatusIsRevoked() {
            MembershipInvitation revoked = MembershipInvitation.reconstitute(
                    MembershipInvitationId.generate(), ORG_ID, EMAIL, ROLE,
                    MembershipInvitationStatus.REVOKED,
                    NOW.minus(Duration.ofHours(1)),
                    NOW.plus(Duration.ofDays(6))
            );

            assertThrows(IllegalStateException.class,
                    () -> revoked.accept(NOW, userId));
        }

        @Test
        @DisplayName("should reject null now")
        void shouldRejectNullNow() {
            assertThrows(NullPointerException.class,
                    () -> pendingInvitation.accept(null, userId));
        }

        @Test
        @DisplayName("should reject null userId")
        void shouldRejectNullUserId() {
            assertThrows(NullPointerException.class,
                    () -> pendingInvitation.accept(NOW, null));
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("issue should reject null organisationId")
        void issueShouldRejectNullOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> MembershipInvitation.issue(null, EMAIL, ROLE, NOW, TTL));
        }

        @Test
        @DisplayName("issue should reject null invitedEmail")
        void issueShouldRejectNullInvitedEmail() {
            assertThrows(NullPointerException.class,
                    () -> MembershipInvitation.issue(ORG_ID, null, ROLE, NOW, TTL));
        }

        @Test
        @DisplayName("issue should reject null role")
        void issueShouldRejectNullRole() {
            assertThrows(NullPointerException.class,
                    () -> MembershipInvitation.issue(ORG_ID, EMAIL, null, NOW, TTL));
        }

        @Test
        @DisplayName("issue should reject null now")
        void issueShouldRejectNullNow() {
            assertThrows(NullPointerException.class,
                    () -> MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, null, TTL));
        }

        @Test
        @DisplayName("issue should reject null ttl")
        void issueShouldRejectNullTtl() {
            assertThrows(NullPointerException.class,
                    () -> MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, NOW, null));
        }

        @Test
        @DisplayName("reconstitute should reject null fields")
        void reconstituteShouldRejectNullFields() {
            MembershipInvitationId id = MembershipInvitationId.generate();
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    null, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.PENDING, NOW, NOW.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, null, EMAIL, ROLE, MembershipInvitationStatus.PENDING, NOW, NOW.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, null, ROLE, MembershipInvitationStatus.PENDING, NOW, NOW.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, null, MembershipInvitationStatus.PENDING, NOW, NOW.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, null, NOW, NOW.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.PENDING, null, NOW.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.PENDING, NOW, null));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two invitations with same id should be equal")
        void twoInvitationsWithSameIdShouldBeEqual() {
            MembershipInvitationId id = MembershipInvitationId.generate();

            MembershipInvitation a = MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.PENDING, NOW, NOW.plus(TTL));
            MembershipInvitation b = MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.ACCEPTED, NOW, NOW.plus(TTL));

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("two invitations with different ids should not be equal")
        void twoInvitationsWithDifferentIdsShouldNotBeEqual() {
            MembershipInvitation a = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, NOW, TTL);
            MembershipInvitation b = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, NOW, TTL);

            assertThat(a).isNotEqualTo(b);
        }
    }
}
