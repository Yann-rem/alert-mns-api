package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant l'activation d'un membre.
 *
 * <p>Le membre passe du statut PENDING à ACTIVE.</p>
 */
public record MemberActivated(
        OrganisationId organisationId,
        MemberId memberId,
        Instant occurredOn
) implements DomainEvent {

    public MemberActivated(OrganisationId organisationId, MemberId memberId) {
        this(organisationId, memberId, Instant.now());
    }
}
