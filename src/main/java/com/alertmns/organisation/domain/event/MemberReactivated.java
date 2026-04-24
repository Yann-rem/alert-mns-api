package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant la réactivation d'un membre suspendu.
 *
 * <p>Le membre passe du statut SUSPENDED à ACTIVE.</p>
 */
public record MemberReactivated(
        OrganisationId organisationId,
        MemberId memberId,
        Instant occurredOn
) implements DomainEvent {

    public MemberReactivated(OrganisationId organisationId, MemberId memberId) {
        this(organisationId, memberId, Instant.now());
    }
}
