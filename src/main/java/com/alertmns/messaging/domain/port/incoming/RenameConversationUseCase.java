package com.alertmns.messaging.domain.port.incoming;

import com.alertmns.messaging.domain.port.incoming.command.RenameConversationCommand;

/**
 * Port entrant représentant le renommage de la conversation d'un groupe.
 *
 * <p>Implémenté par {@link com.alertmns.messaging.application.RenameConversationService}.</p>
 */
public interface RenameConversationUseCase {

    /**
     * Renomme la conversation du groupe. No-op si aucune conversation n'existe pour ce groupe.
     */
    void rename(RenameConversationCommand command);
}
