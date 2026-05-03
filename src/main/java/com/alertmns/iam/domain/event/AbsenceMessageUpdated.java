package com.alertmns.iam.domain.event;

import com.alertmns.shared.UserId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;

/**
 * Événement de domaine représentant la mise à jour du message d'absence d'un utilisateur.
 *
 * <p>Le consommateur doit recharger l'agrégat User pour obtenir le message d'absence mis à jour.</p>
 */
public record AbsenceMessageUpdated(UserId userId, Instant occurredOn) implements DomainEvent {

    public AbsenceMessageUpdated(UserId userId) {
        this(userId, Instant.now());
    }
}
