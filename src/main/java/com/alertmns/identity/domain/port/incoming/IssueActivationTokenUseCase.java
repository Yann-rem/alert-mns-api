package com.alertmns.identity.domain.port.incoming;

import com.alertmns.identity.domain.port.incoming.command.IssueActivationTokenCommand;

/**
 * Port entrant représentant le cas d'utilisation d'émission d'un token d'activation.
 *
 * <p>Implémenté par {@link com.alertmns.identity.application.IssueActivationTokenService}.</p>
 */
public interface IssueActivationTokenUseCase {

    /**
     * Émet un token d'activation à usage unique pour un utilisateur et envoie le lien magique par e-mail.
     *
     * @param command la commande d'émission portant l'identifiant de l'utilisateur cible
     */
    void issue(IssueActivationTokenCommand command);
}
