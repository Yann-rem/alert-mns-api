package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de création d'un groupe dans une organisation.
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VO
 * ({@link com.alertmns.shared.OrganisationId},
 * {@link com.alertmns.organisation.domain.model.GroupName}).</p>
 */
public record CreateGroupCommand(String organisationId, String name) {
    public CreateGroupCommand {
        Objects.requireNonNull(organisationId, "organisationId must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
