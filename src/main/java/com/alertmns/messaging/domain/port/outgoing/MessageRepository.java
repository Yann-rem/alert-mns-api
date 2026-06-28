package com.alertmns.messaging.domain.port.outgoing;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageId;

import java.util.List;
import java.util.Optional;

/**
 * Port sortant pour la persistance des messages.
 */
public interface MessageRepository {

    void save(Message message);

    Optional<Message> findById(MessageId messageId);

    /**
     * Retourne une page de messages d'une conversation, les plus récents d'abord.
     *
     * @param page index de page (commence à 0)
     * @param size taille de page
     */
    List<Message> findByConversationId(ConversationId conversationId, int page, int size);
}
