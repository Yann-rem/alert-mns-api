package com.alertmns.iam.domain.port.outgoing;


import com.alertmns.shared.DomainEvent;

import java.util.List;

/**
 * Port sortant pour la publication des événements du domaine.
 * Implémenté par le bus d'événements dans l'infrastructure.
 */
public interface EventPublisher {

    void publish(List<DomainEvent> events);
}
