package com.alertmns.messaging.domain.event;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant le renommage d'une conversation.
 */
public record ConversationRenamed(
        ConversationId conversationId,
        OrganisationId organisationId,
        ConversationName name,
        Instant occurredOn
) implements DomainEvent {}
