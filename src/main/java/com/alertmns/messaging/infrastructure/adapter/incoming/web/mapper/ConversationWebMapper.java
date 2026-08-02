package com.alertmns.messaging.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.port.incoming.ConversationSummary;
import com.alertmns.messaging.domain.port.incoming.ConversationSummary.LastMessage;
import com.alertmns.messaging.domain.port.incoming.command.CreateDirectConversationCommand;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.ConversationResponse;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.ConversationResponse.LastMessageResponse;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.CreateDirectConversationRequest;

public final class ConversationWebMapper {

    private ConversationWebMapper() {}

    public static CreateDirectConversationCommand toCreateDirectConversationCommand(
            CreateDirectConversationRequest request) {
        return new CreateDirectConversationCommand(request.targetMemberId().toString());
    }

    public static ConversationResponse toConversationResponse(ConversationSummary summary) {
        Conversation conversation = summary.conversation();
        LastMessage last = summary.lastMessage();
        return new ConversationResponse(
                conversation.id().value(),
                conversation.kind(),
                summary.title(),
                conversation.groupId(),
                summary.counterpartMemberId(),
                last == null ? null : new LastMessageResponse(
                        last.messageId(), last.authorId(), last.authorName(), last.content(), last.sentAt()),
                summary.lastActivityAt(),
                conversation.createdAt()
        );
    }
}
