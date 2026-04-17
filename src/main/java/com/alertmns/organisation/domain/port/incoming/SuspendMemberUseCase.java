package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.SuspendMemberCommand;

/**
 * Port entrant représentant le cas d'utilisation de suspension d'un membre.
 *
 * <p>Implémenté par {@link com.alertmns.organisation.application.SuspendMemberService}.</p>
 */
public interface SuspendMemberUseCase {

    /**
     * Suspend un membre actif (ACTIVE → SUSPENDED).
     *
     * @param command la commande de suspension
     */
    void suspend(SuspendMemberCommand command);
}
