package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant le retrait d'un membre d'un groupe.
 */
public record MemberRemovedFromGroup(
        OrganisationId organisationId,
        GroupMembershipId groupMembershipId,
        Instant occurredOn
) implements DomainEvent {}
