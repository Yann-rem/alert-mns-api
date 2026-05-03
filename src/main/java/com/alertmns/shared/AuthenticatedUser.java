package com.alertmns.shared;

import java.util.Objects;

/**
 * Value Object transverse représentant l'identité minimale d'un utilisateur authentifié.
 *
 * <p>Exposé par {@link CurrentUserPort} aux Bounded Contexts qui ont besoin de connaître l'auteur d'une opération.</p>
 *
 * <p>Volontairement minimaliste : seuls {@code userId} et {@code organisationId} y figurent.</p>
 */
public record AuthenticatedUser(UserId userId, OrganisationId organisationId) {

    public AuthenticatedUser {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(organisationId, "organisationId must not be null");
    }
}
