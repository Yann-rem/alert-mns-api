package com.alertmns.identity.domain.event;

import com.alertmns.shared.Email;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.UserId;

import java.time.Instant;

/**
 * Événement de domaine représentant l'inscription d'un nouvel utilisateur.
 *
 * <p>Le compte est créé avec le statut PENDING, en attente d'activation par un administrateur.</p>
 */
public record UserRegistered(UserId userId, Email email, Instant occurredOn) implements DomainEvent {

    public UserRegistered(UserId userId, Email email) {
        this(userId, email, Instant.now());
    }
}
