package com.alertmns.iam.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande contenant les données nécessaires pour enregistrer un nouvel utilisateur.
 * Valeurs brutes — le service applicatif est responsable de la création des VO.
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
