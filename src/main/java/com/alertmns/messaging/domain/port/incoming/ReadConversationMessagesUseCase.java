package com.alertmns.messaging.domain.port.incoming;

import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.port.incoming.command.ReadConversationMessagesQuery;

import java.util.List;

/**
 * Port entrant représentant la lecture paginée des messages d'une conversation.
 *
 * <p>Implémenté par {@link com.alertmns.messaging.application.ReadConversationMessagesService}.</p>
 */
public interface ReadConversationMessagesUseCase {

    List<Message> read(ReadConversationMessagesQuery query);
}
