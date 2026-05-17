package com.alertmns.shared;

import java.util.Objects;

/**
 * Value Object transverse représentant l'identité minimale d'un utilisateur authentifié.
 *
 * <p>Exposé par {@link CurrentUserPort} aux Bounded Contexts qui ont besoin de connaître l'auteur d'une opération.</p>
 *
 * <p>Volontairement minimaliste : seul {@code userId} y figure. Le rattachement organisationnel est porté par
 * l'agrégat {@code Member} côté Organisation BC, pas par l'identité IAM.</p>
 */
public record AuthenticatedUser(UserId userId) {

    public AuthenticatedUser {
        Objects.requireNonNull(userId, "userId must not be null");
    }
}
