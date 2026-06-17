package com.alertmns.messaging.domain.event;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant la création d'une conversation directe.
 */
public record DirectConversationCreated(
        ConversationId conversationId,
        OrganisationId organisationId,
        ParticipantPair participantPair,
        Instant occurredOn
) implements DomainEvent {}
