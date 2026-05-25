package com.alertmns.identity.domain.port.incoming;

import com.alertmns.identity.domain.port.incoming.command.ReactivateUserCommand;

/**
 * Port entrant représentant le cas d'utilisation de réactivation d'un utilisateur.
 *
 * <p>Implémenté par {@link com.alertmns.identity.application.ReactivateUserService}.</p>
 */
public interface ReactivateUserUseCase {

    /**
     * Réactive un utilisateur.
     *
     * @param command la commande de réactivation
     */
    void reactivate(ReactivateUserCommand command);
}
