package com.alertmns.iam.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de réactivation d'un utilisateur.
 *
 * <p>Valeur brute — le service applicatif est responsable de la création du VO.</p>
 */
public record ReactivateUserCommand(String userId) {
    public ReactivateUserCommand {
        Objects.requireNonNull(userId, "userId must not be null");
    }
}
