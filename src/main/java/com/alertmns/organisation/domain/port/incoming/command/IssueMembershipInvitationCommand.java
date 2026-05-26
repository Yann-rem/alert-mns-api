package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande d'émission d'une invitation à rejoindre une organisation.
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VO (parsing + validation d'unicité +
 * check métier).</p>
 */
public record IssueMembershipInvitationCommand(
        String organisationId,
        String invitedEmail,
        String firstName,
        String lastName,
        String role
) {

    public IssueMembershipInvitationCommand {
        Objects.requireNonNull(organisationId, "organisationId must not be null");
        Objects.requireNonNull(invitedEmail, "invitedEmail must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }
}
