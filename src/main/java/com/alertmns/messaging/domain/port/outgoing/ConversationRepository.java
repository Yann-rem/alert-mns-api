package com.alertmns.messaging.domain.port.outgoing;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ParticipantPair;

import java.util.Optional;
import java.util.UUID;

/**
 * Port sortant pour la persistance des conversations.
 */
public interface ConversationRepository {

    void save(Conversation conversation);

    Optional<Conversation> findByGroupId(UUID groupId);

    boolean existsByGroupId(UUID groupId);

    Optional<Conversation> findByParticipants(ParticipantPair participantPair);
}
