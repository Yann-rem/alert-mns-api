package com.alertmns.iam.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de mise à jour du profil d'un utilisateur.
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VO.</p>
 */
public record UpdateProfileCommand(
        String userId,
        String firstName,
        String lastName,
        String avatar
) {
    public UpdateProfileCommand {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
    }
}
