package com.alertmns.messaging.domain.event;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine représentant la création d'une conversation directe.
 */
public record DirectConversationCreated(
        ConversationId conversationId,
        OrganisationId organisationId,
        UUID participantLow,
        UUID participantHigh,
        Instant occurredOn
) implements DomainEvent {}
