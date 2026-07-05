package com.alertmns.messaging.domain.port.incoming;

import com.alertmns.messaging.domain.model.Conversation;

import java.util.List;

/**
 * Port entrant représentant le listage des conversations de l'utilisateur courant.
 *
 * <p>Implémenté par {@link com.alertmns.messaging.application.ListMyConversationsService}.</p>
 */
public interface ListMyConversationsUseCase {

    List<Conversation> list();
}
