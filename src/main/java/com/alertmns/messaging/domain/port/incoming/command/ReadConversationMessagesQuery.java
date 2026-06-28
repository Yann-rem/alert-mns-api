package com.alertmns.messaging.domain.port.incoming.command;

import java.util.Objects;

/**
 * Requête de lecture paginée des messages d'une conversation.
 *
 * <p>{@code page} commence à 0 ; {@code size} est borné à 100 pour protéger la lecture.</p>
 */
public record ReadConversationMessagesQuery(String conversationId, int page, int size) {

    public ReadConversationMessagesQuery {
        Objects.requireNonNull(conversationId, "conversationId must not be null");
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
    }
}
