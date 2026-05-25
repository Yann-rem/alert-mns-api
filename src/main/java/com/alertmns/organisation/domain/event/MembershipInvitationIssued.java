package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.Email;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine signalant qu'une invitation à rejoindre une organisation a été émise.
 */
public record MembershipInvitationIssued(
        MembershipInvitationId invitationId,
        OrganisationId organisationId,
        Email invitedEmail,
        MemberRole role,
        Instant occurredOn
) implements DomainEvent {

    public MembershipInvitationIssued(
            MembershipInvitationId invitationId,
            OrganisationId organisationId,
            Email invitedEmail,
            MemberRole role
    ) {
        this(invitationId, organisationId, invitedEmail, role, Instant.now());
    }
}
