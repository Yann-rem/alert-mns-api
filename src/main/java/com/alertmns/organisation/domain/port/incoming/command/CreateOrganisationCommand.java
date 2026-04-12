package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de création d'une organisation.
 *
 * <p>Valeur brute — le service applicatif est responsable de la création des VO.</p>
 */
public record CreateOrganisationCommand(String name) {

    public CreateOrganisationCommand {
        Objects.requireNonNull(name, "name must not be null");
    }
}
