package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;

/**
 * Port entrant représentant le cas d'utilisation d'invitation d'un membre dans une organisation.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.InviteMemberService}.</p>
 */
public interface InviteMemberUseCase {

    /**
     * Invite un utilisateur à rejoindre une organisation avec un rôle donné.
     *
     * @param command la commande d'invitation
     * @return l'identifiant du membre créé
     */
    MemberId invite(InviteMemberCommand command);
}
