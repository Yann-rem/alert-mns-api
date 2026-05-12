package com.alertmns.iam.domain.exception;

import com.alertmns.iam.domain.model.ActivationTokenId;

/**
 * Exception de domaine représentant un token d'activation expiré.
 *
 * <p>Levée par {@code ActivationToken#verifyUsable()} lorsque l'instant courant dépasse {@code expiresAt}.</p>
 */
public final class ActivationTokenExpiredException extends RuntimeException {
    public ActivationTokenExpiredException(ActivationTokenId id) {
        super("Activation token has expired: " + id.value());
    }
}
