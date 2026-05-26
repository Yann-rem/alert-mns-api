package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant l'acceptation d'une invitation par un utilisateur.
 *
 * <p>Valeurs brutes — le service applicatif convertit en VOs et opère l'orchestration.</p>
 */
public record AcceptMembershipInvitationCommand(String invitationId, String userId) {

    public AcceptMembershipInvitationCommand {
        Objects.requireNonNull(invitationId, "invitationId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
    }
}
