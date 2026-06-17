package com.alertmns.messaging.domain.port.incoming;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.port.incoming.command.CreateDirectConversationCommand;

/**
 * Port entrant représentant la création d'une conversation directe.
 *
 * <p>Implémenté par {@link com.alertmns.messaging.application.CreateDirectConversationService}.</p>
 */
public interface CreateDirectConversationUseCase {

    /**
     * Crée la conversation directe entre l'utilisateur courant et le membre cible, ou retourne celle qui existe déjà.
     *
     * @return l'identifiant de la conversation
     */
    ConversationId create(CreateDirectConversationCommand command);
}
