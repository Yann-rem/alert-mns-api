package com.alertmns.identity.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande d'anonymisation d'un utilisateur (droit à l'effacement RGPD).
 *
 * <p>Valeur brute — le service applicatif est responsable de la création du VO.</p>
 */
public record AnonymizeUserCommand(String userId) {
    public AnonymizeUserCommand {
        Objects.requireNonNull(userId, "userId must not be null");
    }
}
