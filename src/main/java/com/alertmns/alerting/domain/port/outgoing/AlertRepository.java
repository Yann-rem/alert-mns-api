package com.alertmns.alerting.domain.port.outgoing;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertId;

import java.util.Optional;

/**
 * Port sortant de persistence des alertes.
 *
 * <p>Interface dans le domaine, implémentation dans l'infrastructure ({@code AlertPersistenceAdapter}).</p>
 */
public interface AlertRepository {

    /**
     * Persiste une alerte.
     *
     * @param alert l'alerte à sauvegarder
     */
    void save(Alert alert);

    /**
     * Recherche une alerte par son identifiant.
     *
     * @param id l'identifiant recherché
     * @return l'alerte si elle existe
     */
    Optional<Alert> findById(AlertId id);
}
