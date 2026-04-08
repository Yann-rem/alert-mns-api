package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.port.incoming.command.DisableUserCommand;

/**
 * Port entrant représentant le cas d'utilisation de désactivation d'un utilisateur.
 *
 * <p>Implémenté par {@link com.alertmns.iam.application.DisableUserService}.</p>
 */
public interface DisableUserUseCase {

    /**
     * Désactive un utilisateur.
     *
     * @param command la commande de désactivation
     */
    void disable(DisableUserCommand command);
}
