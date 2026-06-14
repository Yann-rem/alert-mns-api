package com.alertmns.messaging.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la création de la conversation adossée à un groupe.
 *
 * <p>Valeur brute — le service applicatif est responsable de la création du VO.</p>
 */
public record CreateConversationFromGroupCommand(String organisationId, String groupId, String name) {

    public CreateConversationFromGroupCommand {
        Objects.requireNonNull(organisationId, "organisationId must not be null");
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
