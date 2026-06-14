package com.alertmns.messaging.infrastructure.adapter.incoming.event;

import com.alertmns.messaging.domain.port.incoming.CreateConversationFromGroupUseCase;
import com.alertmns.messaging.domain.port.incoming.command.CreateConversationFromGroupCommand;
import com.alertmns.organisation.domain.event.GroupCreated;
import org.springframework.context.event.EventListener;

import java.util.Objects;

/**
 * Listener qui crée la conversation d'un groupe dès sa création (cascade {@link GroupCreated} →
 * {@link CreateConversationFromGroupUseCase}).
 */
public final class CreateConversationOnGroupCreatedListener {

    private final CreateConversationFromGroupUseCase createConversationFromGroupUseCase;

    public CreateConversationOnGroupCreatedListener(
            CreateConversationFromGroupUseCase createConversationFromGroupUseCase) {
        this.createConversationFromGroupUseCase = Objects.requireNonNull(
                createConversationFromGroupUseCase, "createConversationFromGroupUseCase must not be null");
    }

    @EventListener
    public void onGroupCreatedEvent(GroupCreated event) {
        createConversationFromGroupUseCase.create(new CreateConversationFromGroupCommand(
                event.organisationId().value().toString(),
                event.groupId().value().toString(),
                event.name().value()
        ));
    }
}
