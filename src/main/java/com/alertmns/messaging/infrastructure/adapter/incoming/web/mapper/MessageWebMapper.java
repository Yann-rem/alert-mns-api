package com.alertmns.messaging.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.port.incoming.MessageView;
import com.alertmns.messaging.domain.port.incoming.command.PostMessageCommand;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.MessageResponse;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.PostMessageRequest;

import java.util.UUID;

public final class MessageWebMapper {

    private MessageWebMapper() {}

    public static PostMessageCommand toPostMessageCommand(UUID conversationId, PostMessageRequest request) {
        UUID replyTo = request.replyToMessageId();
        return new PostMessageCommand(
                conversationId.toString(),
                request.content(),
                replyTo == null ? null : replyTo.toString()
        );
    }

    public static MessageResponse toMessageResponse(MessageView view) {
        Message message = view.message();
        MessageId replyTo = message.replyTo();
        return new MessageResponse(
                message.id().value(),
                message.authorId(),
                view.authorName(),
                message.content().value(),
                replyTo == null ? null : replyTo.value(),
                message.sentAt()
        );
    }
}
