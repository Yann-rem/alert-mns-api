package com.alertmns.iam.domain.port.incoming.query;

import java.util.Objects;

/**
 * Query représentant la demande de validation d'un token d'activation.
 *
 * <p>Valeur brute — le service applicatif est responsable de la création du VO {@code RawToken}.</p>
 */
public record ValidateActivationTokenQuery(String rawToken) {

    public ValidateActivationTokenQuery {
        Objects.requireNonNull(rawToken, "rawToken must not be null");
    }
}
