package com.alertmns.iam.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande d'activation d'un utilisateur.
 *
 * <p>Valeur brute — le service applicatif est responsable de la création du VO.</p>
 */
public record ActivateUserCommand(String userId) {
    public ActivateUserCommand {
        Objects.requireNonNull(userId, "userId must not be null");
    }
}
