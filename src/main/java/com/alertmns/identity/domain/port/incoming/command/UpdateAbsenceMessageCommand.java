package com.alertmns.identity.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de mise à jour du message d'absence d'un utilisateur.
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VO.</p>
 */
public record UpdateAbsenceMessageCommand(String userId, String content, boolean active) {
    public UpdateAbsenceMessageCommand {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(content, "content must not be null");
    }
}
