package com.alertmns.messaging.domain.port.outgoing;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ParticipantPair;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port sortant pour la persistance des conversations.
 */
public interface ConversationRepository {

    void save(Conversation conversation);

    Optional<Conversation> findById(ConversationId conversationId);

    Optional<Conversation> findByGroupId(UUID groupId);

    boolean existsByGroupId(UUID groupId);

    Optional<Conversation> findByParticipants(ParticipantPair participantPair);

    /**
     * Liste les conversations directes auxquelles le membre participe.
     */
    List<Conversation> findByParticipant(UUID memberId);

    /**
     * Liste les conversations de groupe rattachées aux groupes donnés.
     */
    List<Conversation> findByGroupIdIn(Collection<UUID> groupIds);
}
