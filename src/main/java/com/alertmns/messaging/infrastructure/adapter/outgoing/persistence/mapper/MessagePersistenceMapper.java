package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageContent;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.MessageJpaEntity;

import java.util.UUID;

public final class MessagePersistenceMapper {

    private MessagePersistenceMapper() {}

    public static Message toDomain(MessageJpaEntity entity) {
        UUID replyTo = entity.getReplyTo();
        return Message.reconstitute(
                MessageId.from(entity.getId()),
                ConversationId.from(entity.getConversationId()),
                entity.getAuthorId(),
                MessageContent.of(entity.getContent()),
                replyTo == null ? null : MessageId.from(replyTo),
                entity.getSentAt()
        );
    }

    public static MessageJpaEntity toEntity(Message domain) {
        MessageId replyTo = domain.replyTo();
        return new MessageJpaEntity(
                domain.id().value(),
                domain.conversationId().value(),
                domain.authorId(),
                domain.content().value(),
                replyTo == null ? null : replyTo.value(),
                domain.sentAt()
        );
    }
}
