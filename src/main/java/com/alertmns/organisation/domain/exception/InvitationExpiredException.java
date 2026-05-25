package com.alertmns.organisation.domain.exception;

import com.alertmns.shared.MembershipInvitationId;

/**
 * Exception de domaine signalant qu'une tentative d'acceptation a été faite sur une invitation dont le TTL est écoulé.
 */
public final class InvitationExpiredException extends RuntimeException {
    public InvitationExpiredException(MembershipInvitationId id) {
        super("Membership invitation has expired: " + id.value());
    }
}
