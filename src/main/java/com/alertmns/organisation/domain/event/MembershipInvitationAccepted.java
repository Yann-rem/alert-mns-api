package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine signalant qu'une invitation a été acceptée et qu'un {@code Member} doit exister pour le couple
 * {@code (organisationId, userId)} avec le rôle de l'invitation.
 */
public record MembershipInvitationAccepted(
        MembershipInvitationId invitationId,
        OrganisationId organisationId,
        UUID userId,
        MemberRole role,
        Instant occurredOn
) implements DomainEvent {}
