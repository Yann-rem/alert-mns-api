package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {

    List<MessageJpaEntity> findByConversationIdOrderBySentAtDesc(UUID conversationId, Pageable pageable);

    /**
     * Dernier message de chaque conversation citée, en une seule requête.
     *
     * <p>Le départage se fait sur {@code sentAt} : deux messages exactement simultanés dans une même
     * conversation renverraient deux lignes, que l'adaptateur réduit à une.</p>
     */
    @Query("""
            select m from MessageJpaEntity m
            where m.conversationId in :conversationIds
              and m.sentAt = (
                  select max(latest.sentAt) from MessageJpaEntity latest
                  where latest.conversationId = m.conversationId
              )
            """)
    List<MessageJpaEntity> findLastPerConversation(@Param("conversationIds") Collection<UUID> conversationIds);
}
