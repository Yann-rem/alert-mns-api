package com.alertmns.identity.domain.exception;

import com.alertmns.shared.UserId;

/**
 * Exception de domaine représentant l'absence d'un utilisateur recherché par son identifiant.
 */
public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UserId id) {
        super("User not found: " + id.value());
    }
}
