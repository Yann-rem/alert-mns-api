package com.alertmns.iam.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande d'enregistrement d'un nouvel utilisateur.
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VO.</p>
 */
public record RegisterUserCommand(
        String email,
        String rawPassword,
        String firstName,
        String lastName
) {
    public RegisterUserCommand {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(rawPassword, "rawPassword must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
    }
}
