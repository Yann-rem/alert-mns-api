package com.alertmns.identity.domain.port.incoming;

import com.alertmns.identity.domain.port.incoming.command.RegisterPendingUserCommand;
import com.alertmns.shared.UserId;

/**
 * Port entrant représentant le cas d'utilisation d'enregistrement d'un utilisateur en attente d'activation (PENDING),
 * sans mot de passe.
 *
 * <p>Implémenté par {@code RegisterPendingUserService}. Émet {@code UserRegistered}, ce qui déclenche le listener
 * {@code IssueActivationTokenOnUserRegisteredListener} (magic-link).</p>
 */
public interface RegisterPendingUserUseCase {

    /**
     * Enregistre un nouvel utilisateur.
     *
     * @param command la commande d'enregistrement
     * @return l'identifiant de l'utilisateur créé
     */
    UserId register(RegisterPendingUserCommand command);
}
