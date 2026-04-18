package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.RenameGroupCommand;

/**
 * Port entrant représentant le cas d'utilisation de renommage d'un groupe.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.RenameGroupService}.</p>
 */
public interface RenameGroupUseCase {

    /**
     * Renomme un groupe existant.
     *
     * @param command la commande de renommage
     */
    void rename(RenameGroupCommand command);
}
