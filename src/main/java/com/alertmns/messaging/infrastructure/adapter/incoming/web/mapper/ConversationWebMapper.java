package com.alertmns.messaging.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.command.CreateDirectConversationCommand;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.ConversationResponse;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.CreateDirectConversationRequest;

public final class ConversationWebMapper {

    private ConversationWebMapper() {}

    public static CreateDirectConversationCommand toCreateDirectConversationCommand(
            CreateDirectConversationRequest request) {
        return new CreateDirectConversationCommand(request.targetMemberId().toString());
    }

    public static ConversationResponse toConversationResponse(Conversation conversation) {
        ConversationName name = conversation.name();
        ParticipantPair pair = conversation.participantPair();
        return new ConversationResponse(
                conversation.id().value(),
                conversation.kind(),
                name == null ? null : name.value(),
                conversation.groupId(),
                pair == null ? null : pair.low(),
                pair == null ? null : pair.high(),
                conversation.createdAt()
        );
    }
}
