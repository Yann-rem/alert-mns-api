package com.alertmns.identity.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de désactivation d'un utilisateur.
 *
 * <p>Valeur brute — le service applicatif est responsable de la création du VO.</p>
 */
public record SuspendUserCommand(String userId) {
    public SuspendUserCommand {
        Objects.requireNonNull(userId, "userId must not be null");
    }
}
