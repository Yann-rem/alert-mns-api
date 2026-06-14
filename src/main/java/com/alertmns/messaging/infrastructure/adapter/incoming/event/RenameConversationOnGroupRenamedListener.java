package com.alertmns.messaging.infrastructure.adapter.incoming.event;

import com.alertmns.messaging.domain.port.incoming.RenameConversationUseCase;
import com.alertmns.messaging.domain.port.incoming.command.RenameConversationCommand;
import com.alertmns.organisation.domain.event.GroupRenamed;
import org.springframework.context.event.EventListener;

import java.util.Objects;

/**
 * Listener qui propage le renommage d'un groupe à sa conversation (cascade {@link GroupRenamed} →
 * {@link RenameConversationUseCase}).
 */
public final class RenameConversationOnGroupRenamedListener {

    private final RenameConversationUseCase renameConversationUseCase;

    public RenameConversationOnGroupRenamedListener(RenameConversationUseCase renameConversationUseCase) {
        this.renameConversationUseCase = Objects.requireNonNull(
                renameConversationUseCase, "renameConversationUseCase must not be null");
    }

    @EventListener
    public void onGroupRenamedEvent(GroupRenamed event) {
        renameConversationUseCase.rename(new RenameConversationCommand(
                event.groupId().value().toString(),
                event.name().value()
        ));
    }
}
