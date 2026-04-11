package com.alertmns.shared;

import java.util.List;

/**
 * Port sortant pour la publication des événements du domaine.
 *
 * <p>Implémenté par le bus d'événements dans l'infrastructure.</p>
 */
public interface EventPublisher {

    /**
     * Publie une liste d'événements de domaine.
     *
     * @param events les événements à publier
     */
    void publish(List<DomainEvent> events);
}
