package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaEntity;
import com.alertmns.shared.OrganisationId;

public final class ConversationPersistenceMapper {

    private ConversationPersistenceMapper() {}

    public static Conversation toDomain(ConversationJpaEntity entity) {
        String rawName = entity.getName();
        return Conversation.reconstitute(
                ConversationId.from(entity.getId()),
                OrganisationId.from(entity.getOrganisationId()),
                entity.getGroupId(),
                rawName == null ? null : ConversationName.of(rawName),
                entity.getKind(),
                entity.getParticipantLow(),
                entity.getParticipantHigh(),
                entity.getCreatedAt()
        );
    }

    public static ConversationJpaEntity toEntity(Conversation domain) {
        ConversationName name = domain.name();
        return new ConversationJpaEntity(
                domain.id().value(),
                domain.organisationId().value(),
                domain.groupId(),
                domain.kind(),
                name == null ? null : name.value(),
                domain.participantLow(),
                domain.participantHigh(),
                domain.createdAt()
        );
    }
}
