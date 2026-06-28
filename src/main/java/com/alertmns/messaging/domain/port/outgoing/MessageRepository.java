package com.alertmns.messaging.domain.port.outgoing;

import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageId;

import java.util.Optional;

/**
 * Port sortant pour la persistance des messages.
 */
public interface MessageRepository {

    void save(Message message);

    Optional<Message> findById(MessageId messageId);
}
