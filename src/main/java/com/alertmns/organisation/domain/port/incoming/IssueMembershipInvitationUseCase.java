package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.exception.InvitationAlreadyPendingException;
import com.alertmns.organisation.domain.port.incoming.command.IssueMembershipInvitationCommand;
import com.alertmns.shared.MembershipInvitationId;

/**
 * Port entrant représentant le cas d'utilisation d'émission d'une invitation à rejoindre une organisation.
 *
 * <p>Implémenté par {@code IssueMembershipInvitationService}.</p>
 */
public interface IssueMembershipInvitationUseCase {

    /**
     * Émet une invitation à rejoindre une organisation pour un email donné.
     *
     * @param command la commande d'émission
     * @return l'identifiant de l'invitation créée
     * @throws InvitationAlreadyPendingException si une invitation PENDING existe déjà pour cet email
     * @throws UnsupportedOperationException     si un User existe déjà avec cet email
     * @throws IllegalArgumentException          si l'un des champs n'est pas un format valide
     */
    MembershipInvitationId issue(IssueMembershipInvitationCommand command);
}
