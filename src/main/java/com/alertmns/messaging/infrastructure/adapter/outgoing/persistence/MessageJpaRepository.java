package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {

    List<MessageJpaEntity> findByConversationIdOrderBySentAtDesc(UUID conversationId, Pageable pageable);
}
