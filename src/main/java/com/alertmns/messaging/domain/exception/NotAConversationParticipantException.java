package com.alertmns.messaging.domain.exception;

import com.alertmns.messaging.domain.model.ConversationId;

import java.util.UUID;

/**
 * Exception de domaine signalant qu'un membre n'est pas autorisé à participer à une conversation.
 */
public final class NotAConversationParticipantException extends RuntimeException {
    public NotAConversationParticipantException(ConversationId conversationId, UUID memberId) {
        super("Participant not found in conversation " + conversationId.value() + ": " + memberId);
    }
}
