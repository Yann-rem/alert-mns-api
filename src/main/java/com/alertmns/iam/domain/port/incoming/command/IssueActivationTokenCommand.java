package com.alertmns.iam.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande d'émission d'un token d'activation.
 *
 * <p>Valeur brute — le service applicatif est responsable de la création du VO.</p>
 */
public record IssueActivationTokenCommand(String userId) {

    public IssueActivationTokenCommand {
        Objects.requireNonNull(userId, "userId must not be null");
    }
}
