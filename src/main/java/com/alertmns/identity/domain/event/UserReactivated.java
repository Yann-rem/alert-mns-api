package com.alertmns.identity.domain.event;

import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.UserId;

import java.time.Instant;

/**
 * Événement de domaine représentant la réactivation d'un compte utilisateur.
 *
 * <p>Le compte passe du statut SUSPENDED à ACTIVE.</p>
 */
public record UserReactivated(UserId userId, Instant occurredOn) implements DomainEvent {}
