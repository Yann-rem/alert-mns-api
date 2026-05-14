package com.alertmns.iam.domain.exception;

import com.alertmns.iam.domain.model.HashedToken;

/**
 * Exception de domaine représentant l'absence d'un token d'activation pour un hash donné.
 *
 * <p>Levée lorsque le hash dérivé du raw token reçu ne correspond à aucun token en base (token jamais émis, déjà
 * consommé et supprimé, ou raw token altéré).</p>
 */
public final class ActivationTokenNotFoundException extends RuntimeException {
    public ActivationTokenNotFoundException(HashedToken hash) {
        super("Activation token not found for hash: " + hash.hex());
    }
}
