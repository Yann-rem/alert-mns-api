package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.ChangeMemberRoleCommand;

/**
 * Port entrant représentant le cas d'utilisation de changement de rôle d'un membre.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.ChangeMemberRoleService}.</p>
 */
public interface ChangeMemberRoleUseCase {

    /**
     * Change le rôle d'un membre.
     *
     * @param command la commande de changement de rôle
     */
    void changeRole(ChangeMemberRoleCommand command);
}
