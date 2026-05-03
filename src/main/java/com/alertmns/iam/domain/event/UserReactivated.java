package com.alertmns.iam.domain.event;

import com.alertmns.shared.UserId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;

/**
 * Événement de domaine représentant la réactivation d'un compte utilisateur.
 *
 * <p>Le compte passe du statut SUSPENDED à ACTIVE.</p>
 */
public record UserReactivated(UserId userId, Instant occurredOn) implements DomainEvent {

    public UserReactivated(UserId userId) {
        this(userId, Instant.now());
    }
}
