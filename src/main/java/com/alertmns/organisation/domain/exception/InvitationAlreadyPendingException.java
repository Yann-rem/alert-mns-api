package com.alertmns.organisation.domain.exception;

import com.alertmns.shared.Email;

/**
 * Exception de domaine signalant qu'une invitation est déjà PENDING pour l'email cible.
 */
public final class InvitationAlreadyPendingException extends RuntimeException {

    public InvitationAlreadyPendingException(Email email) {
        super("A pending invitation already exists for email: " + email.value());
    }
}
