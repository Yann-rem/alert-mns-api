package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaEntity;
import com.alertmns.shared.OrganisationId;

public final class ConversationPersistenceMapper {

    private ConversationPersistenceMapper() {}

    public static Conversation toDomain(ConversationJpaEntity entity) {
        return Conversation.reconstitute(
                ConversationId.from(entity.getId()),
                OrganisationId.from(entity.getOrganisationId()),
                entity.getGroupId(),
                ConversationName.of(entity.getName()),
                entity.getKind(),
                entity.getCreatedAt()
        );
    }

    public static ConversationJpaEntity toEntity(Conversation domain) {
        return new ConversationJpaEntity(
                domain.id().value(),
                domain.organisationId().value(),
                domain.groupId(),
                domain.kind(),
                domain.name().value(),
                domain.createdAt()
        );
    }
}
