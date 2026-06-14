package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.mapper.ConversationPersistenceMapper;

import java.util.Optional;
import java.util.UUID;

public final class ConversationPersistenceAdapter implements ConversationRepository {

    private final ConversationJpaRepository jpaRepository;

    public ConversationPersistenceAdapter(ConversationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Conversation conversation) {
        jpaRepository.save(ConversationPersistenceMapper.toEntity(conversation));
    }

    @Override
    public Optional<Conversation> findByGroupId(UUID groupId) {
        return jpaRepository.findByGroupId(groupId).map(ConversationPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByGroupId(UUID groupId) {
        return jpaRepository.existsByGroupId(groupId);
    }
}
