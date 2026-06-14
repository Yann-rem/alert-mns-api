package com.alertmns.messaging.domain.port.incoming;

import com.alertmns.messaging.domain.port.incoming.command.CreateConversationFromGroupCommand;

/**
 * Port entrant représentant la création de la conversation d'un groupe.
 *
 * <p>Implémenté par {@link com.alertmns.messaging.application.CreateConversationFromGroupService}.</p>
 */
public interface CreateConversationFromGroupUseCase {

    /**
     * Crée la conversation du groupe si elle n'existe pas déjà.
     */
    void create(CreateConversationFromGroupCommand command);
}
