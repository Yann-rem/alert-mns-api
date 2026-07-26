package com.alertmns.organisation.domain.exception;

import com.alertmns.shared.Email;

/**
 * Exception de domaine signalant qu'un compte existe déjà pour l'email invité.
 *
 * <p>Le MVP ne sait pas rattacher un utilisateur existant à une organisation (cas B) : la personne
 * est soit déjà membre, soit déjà invitée. C'est une <b>limitation métier</b>, exprimée par une
 * exception du domaine plutôt que par un {@code UnsupportedOperationException} — cette dernière
 * signale un défaut de programmation et se traduisait par une erreur 500 sur une action
 * d'administration pourtant courante.</p>
 */
public final class InvitedUserAlreadyExistsException extends RuntimeException {

    public InvitedUserAlreadyExistsException(Email email) {
        super("A user account already exists for email: " + email.value());
    }
}
