package com.alertmns.messaging.domain.exception;

import com.alertmns.messaging.domain.model.ConversationId;

/**
 * Exception de domaine représentant l'absence d'une conversation recherchée par son identifiant.
 */
public final class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(ConversationId id) {
        super("Conversation not found: " + id.value());
    }
}
