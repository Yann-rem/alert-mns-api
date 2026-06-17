package com.alertmns.messaging.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la création d'une conversation directe avec un membre cible.
 */
public record CreateDirectConversationCommand(String targetMemberId) {

    public CreateDirectConversationCommand {
        Objects.requireNonNull(targetMemberId, "targetMemberId must not be null");
    }
}
