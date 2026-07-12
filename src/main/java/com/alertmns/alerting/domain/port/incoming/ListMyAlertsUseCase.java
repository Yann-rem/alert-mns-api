package com.alertmns.alerting.domain.port.incoming;

import com.alertmns.alerting.domain.model.Alert;

import java.util.List;

/**
 * Port entrant listant les alertes destinées à l'utilisateur courant.
 *
 * <p>Implémenté par {@link com.alertmns.alerting.application.ListMyAlertsService}.</p>
 */
public interface ListMyAlertsUseCase {

    /**
     * Retourne les alertes destinées à l'utilisateur courant (toute l'organisation et ses groupes), de la plus
     * récente à la plus ancienne.
     *
     * @return la liste des alertes
     */
    List<Alert> list();
}
