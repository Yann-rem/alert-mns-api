package com.alertmns.alerting.domain.port.incoming;

import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.port.incoming.command.BroadcastAlertCommand;

/**
 * Port entrant représentant la diffusion d'une alerte.
 *
 * <p>Implémenté par {@link com.alertmns.alerting.application.BroadcastAlertService}.</p>
 */
public interface BroadcastAlertUseCase {

    /**
     * Diffuse une alerte au nom du membre courant.
     *
     * @param command les données de diffusion
     * @return l'identifiant de l'alerte diffusée
     */
    AlertId broadcast(BroadcastAlertCommand command);
}
