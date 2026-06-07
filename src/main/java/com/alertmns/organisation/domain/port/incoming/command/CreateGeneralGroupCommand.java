package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de création du groupe GENERAL d'une organisation.
 *
 * <p>Valeurs brutes — le service applicatif crée les VO.</p>
 */
public record CreateGeneralGroupCommand(String organisationId, String name) {

    public CreateGeneralGroupCommand {
        Objects.requireNonNull(organisationId, "organisationId must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
