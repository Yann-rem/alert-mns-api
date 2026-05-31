package com.alertmns.identity.domain.event;

import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.UserId;

import java.time.Instant;

/**
 * Événement de domaine représentant la mise à jour du profil d'un utilisateur.
 *
 * <p>Le consommateur doit recharger l'agrégat User pour obtenir le profil mis à jour.</p>
 */
public record ProfileUpdated(UserId userId, Instant occurredOn) implements DomainEvent {}
