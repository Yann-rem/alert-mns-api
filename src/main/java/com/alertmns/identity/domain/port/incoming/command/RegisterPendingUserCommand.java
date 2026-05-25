package com.alertmns.identity.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande d'enregistrement d'un utilisateur en attente d'activation.
 *
 * <p>Aucun mot de passe : il sera défini ultérieurement via le magic-link.</p>
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VO.</p>
 */
public record RegisterPendingUserCommand(
        String email,
        String firstName,
        String lastName
) {
    public RegisterPendingUserCommand {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
    }
}
