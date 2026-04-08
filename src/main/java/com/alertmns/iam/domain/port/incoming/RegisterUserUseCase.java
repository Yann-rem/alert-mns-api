package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;

/**
 * Port entrant pour l'enregistrement d'un utilisateur.
 * Implémenté par RegisterUserService dans la couche applicative.
 */
public interface RegisterUserUseCase {

    UserId register(RegisterUserCommand command);
}
