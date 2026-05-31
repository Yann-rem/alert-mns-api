package com.alertmns.identity.domain.event;

import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.Email;
import com.alertmns.shared.UserId;

import java.time.Instant;

/**
 * Événement de domaine représentant l'activation d'un compte utilisateur.
 *
 * <p>Le compte passe du statut PENDING à ACTIVE.</p>
 */
public record UserActivated(UserId userId, Email email, Instant occurredOn) implements DomainEvent {}
