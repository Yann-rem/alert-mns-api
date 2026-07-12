package com.alertmns.messaging.infrastructure.adapter.incoming.ws;

import java.util.UUID;

/**
 * Signal éphémère « un utilisateur est en train d'écrire » relayé aux autres participants d'une conversation.
 *
 * <p>Volontairement minimal : {@code conversationId} + {@code userId} du typist. Le front, qui connaît déjà les
 * participants de la conversation, résout le nom localement — pas de lookup serveur par frappe. N'est jamais persisté.</p>
 */
public record TypingNotification(UUID conversationId, UUID userId) {}
