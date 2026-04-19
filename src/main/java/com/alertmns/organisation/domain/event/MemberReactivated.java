package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;

/**
 * Événement de domaine représentant la réactivation d'un membre suspendu.
 *
 * <p>Le membre passe du statut SUSPENDED à ACTIVE.</p>
 */
public record MemberReactivated(MemberId memberId, Instant occurredOn) implements DomainEvent {

    public MemberReactivated(MemberId memberId) {
        this(memberId, Instant.now());
    }
}
