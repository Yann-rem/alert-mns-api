package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de changement de rôle d'un membre.
 *
 * <p>Valeurs brutes — le service applicatif crée les VO et interprète {@code role}.</p>
 *
 * @param organisationId identifiant de l'organisation
 * @param memberId       identifiant du membre ciblé
 * @param role           nouveau rôle ({@code ADMIN}, {@code MANAGER} ou {@code MEMBER})
 */
public record ChangeMemberRoleCommand(String organisationId, String memberId, String role) {
    public ChangeMemberRoleCommand {
        Objects.requireNonNull(organisationId, "organisationId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }
}
