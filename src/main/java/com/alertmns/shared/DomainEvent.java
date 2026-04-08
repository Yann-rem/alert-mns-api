package com.alertmns.shared;

import java.time.Instant;

/**
 * Contrat représentant un événement de domaine.
 *
 * <p>Un événement de domaine représente un fait métier qui s'est produit.
 * Les implémentations doivent être immuables.</p>
 */
public interface DomainEvent {

    /**
     * Retourne l'instant auquel l'événement s'est produit.
     *
     * @return l'instant de l'événement
     */
    Instant occurredOn();
}
