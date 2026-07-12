package com.alertmns.alerting.domain.port.outgoing;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.shared.OrganisationId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Retourne les alertes visant toute l'organisation.
     *
     * @param organisationId l'organisation
     * @return les alertes d'audience ORGANISATION de cette organisation
     */
    List<Alert> findOrganisationWide(OrganisationId organisationId);

    /**
     * Retourne les alertes visant l'un des groupes donnés.
     *
     * @param groupIds identifiants de groupes
     * @return les alertes d'audience GROUP dont le groupe figure dans {@code groupIds}
     */
    List<Alert> findByGroupIdIn(Collection<UUID> groupIds);
}
