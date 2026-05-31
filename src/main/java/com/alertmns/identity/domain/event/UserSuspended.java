package com.alertmns.identity.domain.event;

import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.UserId;

import java.time.Instant;

/**
 * Événement de domaine représentant la suspension d'un compte utilisateur.
 *
 * <p>Le compte passe du statut ACTIVE à SUSPENDED.</p>
 */
public record UserSuspended(UserId userId, Instant occurredOn) implements DomainEvent {}
