package com.alertmns.iam.domain.event;

import com.alertmns.shared.UserId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;

/**
 * Événement de domaine représentant l'activation d'un compte utilisateur.
 *
 * <p>Le compte passe du statut PENDING à ACTIVE.</p>
 */
public record UserActivated(UserId userId, Instant occurredOn) implements DomainEvent {

    public UserActivated(UserId userId) {
        this(userId, Instant.now());
    }
}
