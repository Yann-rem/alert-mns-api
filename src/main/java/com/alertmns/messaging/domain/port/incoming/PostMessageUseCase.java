package com.alertmns.messaging.domain.port.incoming;

import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.port.incoming.command.PostMessageCommand;

/**
 * Port entrant représentant l'envoi d'un message dans une conversation.
 *
 * <p>Implémenté par {@link com.alertmns.messaging.application.PostMessageService}.</p>
 */
public interface PostMessageUseCase {

    MessageId post(PostMessageCommand command);
}
