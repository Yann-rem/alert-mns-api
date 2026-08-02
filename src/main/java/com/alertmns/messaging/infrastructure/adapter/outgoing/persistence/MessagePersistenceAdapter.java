package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.mapper.MessagePersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public List<Message> findByConversationId(ConversationId conversationId, int page, int size) {
        return jpaRepository
                .findByConversationIdOrderBySentAtDesc(conversationId.value(), PageRequest.of(page, size))
                .stream()
                .map(MessagePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Map<ConversationId, Message> findLastMessagePerConversation(Collection<ConversationId> conversationIds) {
        if (conversationIds.isEmpty()) {
            return Map.of();
        }

        List<UUID> rawIds = conversationIds.stream().map(ConversationId::value).toList();
        return jpaRepository.findLastPerConversation(rawIds).stream()
                .map(MessagePersistenceMapper::toDomain)
                // Deux messages exactement simultanés dans une conversation : on n'en garde qu'un.
                .collect(Collectors.toMap(
                        Message::conversationId, Function.identity(), (first, ignored) -> first));
    }
}
