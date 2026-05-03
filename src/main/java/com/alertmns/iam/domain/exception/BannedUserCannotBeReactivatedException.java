package com.alertmns.iam.domain.exception;

import com.alertmns.iam.domain.model.UserId;

/**
 * Exception de domaine représentant le refus de réactiver un utilisateur banni.
 *
 * <p>Le statut {@code BANNED} est terminal — aucune transition vers {@code ACTIVE} n'est autorisée.</p>
 */
public final class BannedUserCannotBeReactivatedException extends RuntimeException {
    public BannedUserCannotBeReactivatedException(UserId id) {
        super("Banned user cannot be reactivated: " + id.value());
    }
}
