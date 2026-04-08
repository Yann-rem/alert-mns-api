package com.alertmns.iam.domain.exception;

import com.alertmns.iam.domain.model.UserId;

/**
 * Exception de domaine représentant l'absence d'un utilisateur recherché par son identifiant.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UserId id) {
        super("User not found: " + id.value());
    }
}
