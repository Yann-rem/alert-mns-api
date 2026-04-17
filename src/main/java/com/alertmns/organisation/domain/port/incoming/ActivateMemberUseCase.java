package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.ActivateMemberCommand;

/**
 * Port entrant représentant le cas d'utilisation d'activation d'un membre.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.ActivateMemberService}.</p>
 */
public interface ActivateMemberUseCase {

    /**
     * Active un membre en attente (PENDING → ACTIVE).
     *
     * @param command la commande d'activation
     */
    void activate(ActivateMemberCommand command);
}
