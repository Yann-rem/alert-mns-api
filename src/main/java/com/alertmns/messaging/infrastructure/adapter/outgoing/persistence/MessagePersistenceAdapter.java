package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence;

import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.mapper.MessagePersistenceMapper;

import java.util.Optional;

public final class MessagePersistenceAdapter implements MessageRepository {

    private final MessageJpaRepository jpaRepository;

    public MessagePersistenceAdapter(MessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Message message) {
        jpaRepository.save(MessagePersistenceMapper.toEntity(message));
    }

    @Override
    public Optional<Message> findById(MessageId messageId) {
        return jpaRepository.findById(messageId.value()).map(MessagePersistenceMapper::toDomain);
    }
}
