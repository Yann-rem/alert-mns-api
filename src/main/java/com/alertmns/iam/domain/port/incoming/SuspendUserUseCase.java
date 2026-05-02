package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.application.SuspendUserService;
import com.alertmns.iam.domain.port.incoming.command.SuspendUserCommand;

/**
 * Port entrant représentant le cas d'utilisation de désactivation d'un utilisateur.
 *
 * <p>Implémenté par {@link SuspendUserService}.</p>
 */
public interface SuspendUserUseCase {

    /**
     * Désactive un utilisateur.
     *
     * @param command la commande de désactivation
     */
    void suspend(SuspendUserCommand command);
}
