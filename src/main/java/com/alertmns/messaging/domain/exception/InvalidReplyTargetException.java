package com.alertmns.messaging.domain.exception;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.MessageId;

/**
 * Exception de domaine signalant qu'une réponse cible un message invalide : inexistant, ou appartenant à une autre
 * conversation.
 */
public final class InvalidReplyTargetException extends RuntimeException {

    private InvalidReplyTargetException(String message) {
        super(message);
    }

    public static InvalidReplyTargetException notFound(MessageId replyTo) {
        return new InvalidReplyTargetException("Reply target not found: " + replyTo.value());
    }

    public static InvalidReplyTargetException notInConversation(ConversationId conversationId, MessageId replyTo) {
        return new InvalidReplyTargetException(
                "Reply target does not belong to conversation " + conversationId.value() + ": " + replyTo.value());
    }
}
