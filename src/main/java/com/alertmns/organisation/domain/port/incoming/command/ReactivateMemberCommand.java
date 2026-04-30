package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de réactivation d'un membre suspendu.
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VO
 * ({@link com.alertmns.shared.OrganisationId},
 * {@link com.alertmns.organisation.domain.model.MemberId}).</p>
 */
public record ReactivateMemberCommand(String organisationId, String memberId) {
    public ReactivateMemberCommand {
        Objects.requireNonNull(organisationId, "organisationId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
    }
}
