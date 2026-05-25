package com.alertmns.identity.domain.exception;

import com.alertmns.identity.domain.model.Email;

/**
 * Exception de domaine représentant un conflit d'email déjà associé à un compte existant.
 */
public final class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(Email email) {
        super("Email already exists: " + email.value());
    }
}
