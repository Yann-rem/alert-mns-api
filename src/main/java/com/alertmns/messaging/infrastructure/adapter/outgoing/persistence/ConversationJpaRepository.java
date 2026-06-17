package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationJpaRepository extends JpaRepository<ConversationJpaEntity, UUID> {

    Optional<ConversationJpaEntity> findByGroupId(UUID groupId);

    boolean existsByGroupId(UUID groupId);

    Optional<ConversationJpaEntity> findByParticipantLowAndParticipantHigh(UUID participantLow, UUID participantHigh);
}
