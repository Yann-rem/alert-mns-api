package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.port.incoming.command.DisableUserCommand;

/**
 * Port entrant pour la désactivation d'un utilisateur.
 * Implémenté par DisableUserService dans la couche applicative.
 */
public interface DisableUserUseCase {

    void disable(DisableUserCommand command);
}
