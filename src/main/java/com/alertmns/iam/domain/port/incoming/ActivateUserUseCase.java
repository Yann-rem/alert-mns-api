package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.port.incoming.command.ActivateUserCommand;

/**
 * Port entrant pour l'activation d'un utilisateur.
 * Implémenté par ActivateUserService dans la couche applicative.
 */
public interface ActivateUserUseCase {

    void activate(ActivateUserCommand command);
}
