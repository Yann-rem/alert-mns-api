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

    @Nested
    @DisplayName("Issuance")
    class Issuance {

        @Test
        @DisplayName("should create an invitation in PENDING status")
        void shouldCreateAnInvitationInPendingStatus() {
            MembershipInvitation invitation = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, TTL);

            assertEquals(MembershipInvitationStatus.PENDING, invitation.status());
            assertEquals(ORG_ID, invitation.organisationId());
            assertEquals(EMAIL, invitation.invitedEmail());
            assertEquals(ROLE, invitation.role());
            assertNotNull(invitation.id());
            assertNotNull(invitation.createdAt());
        }

        @Test
        @DisplayName("should set expiresAt = createdAt + ttl")
        void shouldSetExpiresAtBasedOnTtl() {
            MembershipInvitation invitation = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, TTL);

            // Allow for small drift between Instant.now() calls in the factory and the assertion.
            Duration actualWindow = Duration.between(invitation.createdAt(), invitation.expiresAt());
            assertEquals(TTL, actualWindow);
        }

        @Test
        @DisplayName("should emit MembershipInvitationIssued")
        void shouldEmitMembershipInvitationIssued() {
            MembershipInvitation invitation = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, TTL);

            List<DomainEvent> events = invitation.pullDomainEvents();
            assertEquals(1, events.size());
            MembershipInvitationIssued event = assertInstanceOf(
                    MembershipInvitationIssued.class, events.getFirst());
            assertEquals(invitation.id(), event.invitationId());
            assertEquals(ORG_ID, event.organisationId());
            assertEquals(EMAIL, event.invitedEmail());
            assertEquals(ROLE, event.role());
        }
    }

    @Nested
    @DisplayName("Reconstitution")
    class Reconstitution {

        @Test
        @DisplayName("should reconstitute without emitting any event")
        void shouldReconstituteWithoutEmittingAnyEvent() {
            MembershipInvitationId id = MembershipInvitationId.generate();
            Instant createdAt = Instant.now();
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
            pendingInvitation = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, TTL);
            pendingInvitation.pullDomainEvents(); // clear MembershipInvitationIssued
            userId = UUID.randomUUID();
        }

        @Test
        @DisplayName("should transition PENDING to ACCEPTED")
        void shouldTransitionPendingToAccepted() {
            pendingInvitation.accept(Instant.now(), userId);

            assertEquals(MembershipInvitationStatus.ACCEPTED, pendingInvitation.status());
        }

        @Test
        @DisplayName("should emit MembershipInvitationAccepted with userId and role")
        void shouldEmitMembershipInvitationAccepted() {
            pendingInvitation.accept(Instant.now(), userId);

            List<DomainEvent> events = pendingInvitation.pullDomainEvents();
            assertEquals(1, events.size());
            MembershipInvitationAccepted event = assertInstanceOf(
                    MembershipInvitationAccepted.class, events.getFirst());
            assertEquals(pendingInvitation.id(), event.invitationId());
            assertEquals(ORG_ID, event.organisationId());
            assertEquals(userId, event.userId());
            assertEquals(ROLE, event.role());
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
            pendingInvitation.accept(Instant.now(), userId);
            pendingInvitation.pullDomainEvents();

            assertThrows(IllegalStateException.class,
                    () -> pendingInvitation.accept(Instant.now(), UUID.randomUUID()));
        }

        @Test
        @DisplayName("should throw IllegalStateException when status is EXPIRED")
        void shouldThrowWhenStatusIsExpired() {
            MembershipInvitation expired = MembershipInvitation.reconstitute(
                    MembershipInvitationId.generate(), ORG_ID, EMAIL, ROLE,
                    MembershipInvitationStatus.EXPIRED,
                    Instant.now().minus(Duration.ofDays(10)),
                    Instant.now().minus(Duration.ofDays(3))
            );

            assertThrows(IllegalStateException.class,
                    () -> expired.accept(Instant.now(), userId));
        }

        @Test
        @DisplayName("should throw IllegalStateException when status is REVOKED")
        void shouldThrowWhenStatusIsRevoked() {
            MembershipInvitation revoked = MembershipInvitation.reconstitute(
                    MembershipInvitationId.generate(), ORG_ID, EMAIL, ROLE,
                    MembershipInvitationStatus.REVOKED,
                    Instant.now().minus(Duration.ofHours(1)),
                    Instant.now().plus(Duration.ofDays(6))
            );

            assertThrows(IllegalStateException.class,
                    () -> revoked.accept(Instant.now(), userId));
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
                    () -> pendingInvitation.accept(Instant.now(), null));
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("issue should reject null organisationId")
        void issueShouldRejectNullOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> MembershipInvitation.issue(null, EMAIL, ROLE, TTL));
        }

        @Test
        @DisplayName("issue should reject null invitedEmail")
        void issueShouldRejectNullInvitedEmail() {
            assertThrows(NullPointerException.class,
                    () -> MembershipInvitation.issue(ORG_ID, null, ROLE, TTL));
        }

        @Test
        @DisplayName("issue should reject null role")
        void issueShouldRejectNullRole() {
            assertThrows(NullPointerException.class,
                    () -> MembershipInvitation.issue(ORG_ID, EMAIL, null, TTL));
        }

        @Test
        @DisplayName("issue should reject null ttl")
        void issueShouldRejectNullTtl() {
            assertThrows(NullPointerException.class,
                    () -> MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, null));
        }

        @Test
        @DisplayName("reconstitute should reject null fields")
        void reconstituteShouldRejectNullFields() {
            Instant now = Instant.now();
            MembershipInvitationId id = MembershipInvitationId.generate();
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    null, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.PENDING, now, now.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, null, EMAIL, ROLE, MembershipInvitationStatus.PENDING, now, now.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, null, ROLE, MembershipInvitationStatus.PENDING, now, now.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, null, MembershipInvitationStatus.PENDING, now, now.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, null, now, now.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.PENDING, null, now.plus(TTL)));
            assertThrows(NullPointerException.class, () -> MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.PENDING, now, null));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two invitations with same id should be equal")
        void twoInvitationsWithSameIdShouldBeEqual() {
            MembershipInvitationId id = MembershipInvitationId.generate();
            Instant now = Instant.now();

            MembershipInvitation a = MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.PENDING, now, now.plus(TTL));
            MembershipInvitation b = MembershipInvitation.reconstitute(
                    id, ORG_ID, EMAIL, ROLE, MembershipInvitationStatus.ACCEPTED, now, now.plus(TTL));

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("two invitations with different ids should not be equal")
        void twoInvitationsWithDifferentIdsShouldNotBeEqual() {
            MembershipInvitation a = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, TTL);
            MembershipInvitation b = MembershipInvitation.issue(ORG_ID, EMAIL, ROLE, TTL);

            assertThat(a).isNotEqualTo(b);
        }
    }
}
