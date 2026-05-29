package com.alertmns.organisation.domain.exception;

import com.alertmns.shared.MembershipInvitationId;

/**
 * Exception de domaine signalant qu'aucune invitation ne correspond à l'identifiant recherché.
 */
public final class MembershipInvitationNotFoundException extends RuntimeException {
    public MembershipInvitationNotFoundException(MembershipInvitationId id) {
        super("Membership invitation not found: " + id.value());
    }
}
