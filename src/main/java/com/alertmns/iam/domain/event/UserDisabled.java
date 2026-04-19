package com.alertmns.iam.domain.event;

import com.alertmns.iam.domain.model.UserId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;

/**
 * Événement de domaine représentant la désactivation d'un compte utilisateur.
 *
 * <p>Le compte passe du statut ACTIVE à DISABLED.</p>
 */
public record UserDisabled(UserId userId, Instant occurredOn) implements DomainEvent {

    public UserDisabled(UserId userId) {
        this(userId, Instant.now());
    }
}
