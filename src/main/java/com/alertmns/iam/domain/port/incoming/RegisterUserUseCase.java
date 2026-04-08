package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;

/**
 * Port entrant représentant le cas d'utilisation d'enregistrement d'un utilisateur.
 *
 * <p>Implémenté par {@link com.alertmns.iam.application.RegisterUserService}.</p>
 */
public interface RegisterUserUseCase {

    /**
     * Enregistre un nouvel utilisateur.
     *
     * @param command la commande d'enregistrement
     * @return l'identifiant de l'utilisateur créé
     */
    UserId register(RegisterUserCommand command);
}
