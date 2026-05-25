package com.alertmns.identity.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de consommation d'un token d'activation.
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VOs ({@code RawToken} pour la clé,
 * {@code HashedPassword} après hachage du {@code rawPassword}).</p>
 */
public record RedeemActivationTokenCommand(String rawToken, String rawPassword) {

    public RedeemActivationTokenCommand {
        Objects.requireNonNull(rawToken, "rawToken must not be null");
        Objects.requireNonNull(rawPassword, "rawPassword must not be null");
    }
}
