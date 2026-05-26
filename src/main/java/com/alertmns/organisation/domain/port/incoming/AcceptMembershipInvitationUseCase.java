package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.exception.InvitationExpiredException;
import com.alertmns.organisation.domain.port.incoming.command.AcceptMembershipInvitationCommand;

/**
 * Port entrant représentant l'acceptation d'une invitation à rejoindre une organisation.
 *
 * <p>Implémenté par {@code AcceptMembershipInvitationService}.</p>
 */
public interface AcceptMembershipInvitationUseCase {

    /**
     * Accepte une invitation en attente et crée le {@code Member} correspondant.
     *
     * @param command la commande d'acceptation
     * @throws InvitationExpiredException si l'invitation est expirée
     * @throws IllegalStateException      si l'invitation n'est pas en statut {@code PENDING}
     * @throws IllegalArgumentException   si {@code invitationId} ou {@code userId} n'est pas un UUID valide
     */
    void accept(AcceptMembershipInvitationCommand command);
}
