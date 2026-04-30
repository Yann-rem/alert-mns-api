package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande d'invitation d'un utilisateur en tant que membre d'une organisation.
 *
 * <p>Valeurs brutes — le service applicatif est responsable du parsing
 * ({@link com.alertmns.shared.OrganisationId}, identifiant utilisateur en {@link java.util.UUID},
 * {@link com.alertmns.organisation.domain.model.MemberRole}).</p>
 */
public record InviteMemberCommand(
        String organisationId,
        String userId,
        String role
) {
    public InviteMemberCommand {
        Objects.requireNonNull(organisationId, "organisationId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }
}
