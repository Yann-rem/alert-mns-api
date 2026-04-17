package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.port.incoming.command.CreateGroupCommand;

/**
 * Port entrant représentant le cas d'utilisation de création d'un groupe au sein d'une organisation.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.CreateGroupService}.</p>
 */
public interface CreateGroupUseCase {

    /**
     * Crée un nouveau groupe dans une organisation.
     *
     * @param command la commande de création
     * @return l'identifiant du groupe créé
     */
    GroupId create(CreateGroupCommand command);
}
