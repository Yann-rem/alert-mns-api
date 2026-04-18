package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.RemoveMemberFromGroupCommand;

/**
 * Port entrant représentant le cas d'utilisation de retrait d'un membre d'un groupe.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.RemoveMemberFromGroupService}.</p>
 */
public interface RemoveMemberFromGroupUseCase {

    /**
     * Retire un membre d'un groupe.
     *
     * @param command la commande de retrait
     */
    void remove(RemoveMemberFromGroupCommand command);
}
