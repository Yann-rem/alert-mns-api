package com.alertmns.shared;

import java.time.Instant;

/**
 * Interface pour les événements de domaine.
 * Un événement de domaine représente quelque chose qui s'est produit dans le domaine.
 * Les implémentations doivent être immuables.
 */
public interface DomainEvent {

    Instant occurredOn();
}
