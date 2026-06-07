package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.CreateGeneralGroupCommand;

/**
 * Port entrant représentant la création (idempotente) du groupe GENERAL d'une organisation.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.CreateGeneralGroupService}.</p>
 */
public interface CreateGeneralGroupUseCase {

    /**
     * Crée le groupe GENERAL de l'organisation s'il n'existe pas déjà (no-op sinon).
     *
     * @param command la commande de création
     */
    void create(CreateGeneralGroupCommand command);
}
