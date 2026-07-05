package com.alertmns.organisation.domain.exception;

/**
 * Exception de domaine : une opération ne peut pas faire passer le nombre d'administrateurs actifs sous 1.
 *
 * <p>Invariant d'opération de l'ADR-0013 (« au moins un ADMIN actif »). Levée lorsqu'on tente de rétrograder (ou, à
 * terme, de suspendre / retirer) le dernier {@code Member} de rôle {@code ADMIN} en statut {@code ACTIVE}.</p>
 */
public final class LastAdminCannotBeRemovedException extends RuntimeException {

    public LastAdminCannotBeRemovedException() {
        super("Operation refused: an organisation must keep at least one active ADMIN");
    }
}
