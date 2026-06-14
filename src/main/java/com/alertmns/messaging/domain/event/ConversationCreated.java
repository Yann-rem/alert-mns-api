package com.alertmns.messaging.domain.event;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine représentant la création d'une conversation.
 */
public record ConversationCreated(
        ConversationId conversationId,
        OrganisationId organisationId,
        UUID groupId,
        ConversationKind kind,
        Instant occurredOn
) implements DomainEvent {}
