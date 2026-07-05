package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant le changement de rôle d'un membre.
 */
public record MemberRoleChanged(
        OrganisationId organisationId,
        MemberId memberId,
        MemberRole newRole,
        Instant occurredOn
) implements DomainEvent {}
