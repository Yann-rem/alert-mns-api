package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;

/**
 * Événement de domaine représentant l'activation d'un membre.
 *
 * <p>Le membre passe du statut PENDING à ACTIVE.</p>
 */
public record MemberActivated(MemberId memberId, Instant occurredOn) implements DomainEvent {

    public MemberActivated(MemberId memberId) {
        this(memberId, Instant.now());
    }
}
