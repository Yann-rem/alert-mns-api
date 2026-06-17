package com.alertmns.messaging.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.messaging.domain.port.incoming.command.CreateDirectConversationCommand;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.CreateDirectConversationRequest;

public final class ConversationWebMapper {

    private ConversationWebMapper() {}

    public static CreateDirectConversationCommand toCreateDirectConversationCommand(
            CreateDirectConversationRequest request) {
        return new CreateDirectConversationCommand(request.targetMemberId().toString());
    }
}
