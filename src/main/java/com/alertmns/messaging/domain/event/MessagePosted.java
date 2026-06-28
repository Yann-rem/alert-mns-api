package com.alertmns.messaging.domain.event;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine représentant l'envoi d'un message dans une conversation.
 */
public record MessagePosted(
        MessageId messageId,
        ConversationId conversationId,
        UUID authorId,
        Instant occurredOn
) implements DomainEvent {}
