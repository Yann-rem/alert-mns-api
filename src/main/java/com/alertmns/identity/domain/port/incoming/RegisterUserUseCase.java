package com.alertmns.identity.domain.port.incoming;

import com.alertmns.shared.UserId;
import com.alertmns.identity.domain.port.incoming.command.RegisterUserCommand;

/**
 * Port entrant représentant le cas d'utilisation d'enregistrement d'un nouvel utilisateur.
 *
 * <p>Implémenté par {@link com.alertmns.identity.application.RegisterUserService}.</p>
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
