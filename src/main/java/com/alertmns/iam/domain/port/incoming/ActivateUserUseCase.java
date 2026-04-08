package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.port.incoming.command.ActivateUserCommand;

/**
 * Port entrant représentant le cas d'utilisation d'activation d'un utilisateur.
 *
 * <p>Implémenté par {@link com.alertmns.iam.application.ActivateUserService}.</p>
 */
public interface ActivateUserUseCase {

    /**
     * Active un utilisateur.
     *
     * @param command la commande d'activation
     */
    void activate(ActivateUserCommand command);
}
