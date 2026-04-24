package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant l'ajout d'un membre à un groupe.
 */
public record MemberAddedToGroup(
        OrganisationId organisationId,
        GroupMembershipId groupMembershipId,
        Instant occurredOn
) implements DomainEvent {

    public MemberAddedToGroup(OrganisationId organisationId, GroupMembershipId groupMembershipId) {
        this(organisationId, groupMembershipId, Instant.now());
    }
}
