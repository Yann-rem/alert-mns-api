package com.alertmns.messaging.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant le renommage de la conversation d'un groupe.
 *
 * <p>Valeur brute — le service applicatif est responsable de la création du VO.</p>
 */
public record RenameConversationCommand(String groupId, String name) {

    public RenameConversationCommand {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
