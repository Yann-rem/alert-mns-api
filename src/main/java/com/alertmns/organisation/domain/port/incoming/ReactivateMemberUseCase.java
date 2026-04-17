package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.ReactivateMemberCommand;

/**
 * Port entrant représentant le cas d'utilisation de réactivation d'un membre suspendu.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.ReactivateMemberService}.</p>
 */
public interface ReactivateMemberUseCase {

    /**
     * Réactive un membre suspendu (SUSPENDED → ACTIVE).
     *
     * @param command la commande de réactivation
     */
    void reactivate(ReactivateMemberCommand command);
}
