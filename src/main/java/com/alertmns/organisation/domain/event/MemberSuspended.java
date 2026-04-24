package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant la suspension d'un membre.
 *
 * <p>Le membre passe du statut ACTIVE à SUSPENDED.</p>
 */
public record MemberSuspended(
        OrganisationId organisationId,
        MemberId memberId,
        Instant occurredOn
) implements DomainEvent {

    public MemberSuspended(OrganisationId organisationId, MemberId memberId) {
        this(organisationId, memberId, Instant.now());
    }
}
