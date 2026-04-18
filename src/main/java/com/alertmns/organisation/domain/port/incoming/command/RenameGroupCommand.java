package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de renommage d'un groupe.
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VO
 * ({@link com.alertmns.organisation.domain.model.GroupId},
 * {@link com.alertmns.organisation.domain.model.GroupName}).</p>
 */
public record RenameGroupCommand(String groupId, String name) {
    public RenameGroupCommand {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
