package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.AddMemberToGroupCommand;

/**
 * Port entrant représentant le cas d'utilisation d'ajout d'un membre à un groupe.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.AddMemberToGroupService}.</p>
 */
public interface AddMemberToGroupUseCase {

    /**
     * Ajoute un membre à un groupe de la même organisation.
     *
     * @param command la commande d'ajout
     */
    void add(AddMemberToGroupCommand command);
}
