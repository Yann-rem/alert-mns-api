package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant l'invitation d'un membre dans une organisation.
 *
 * <p>Le membre est créé avec le statut PENDING, en attente d'activation.</p>
 */
public record MemberInvited(
        OrganisationId organisationId,
        MemberId memberId,
        Instant occurredOn
) implements DomainEvent {

    public MemberInvited(OrganisationId organisationId, MemberId memberId) {
        this(organisationId, memberId, Instant.now());
    }
}
