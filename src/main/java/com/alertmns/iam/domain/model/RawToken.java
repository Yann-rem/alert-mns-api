package com.alertmns.iam.domain.model;

import java.util.Objects;

/**
 * Value Object représentant un jeton d'activation brut (non haché).
 *
 * <p>Le token est généré aléatoirement côté application puis transmis à l'utilisateur via le lien magique. Il n'est
 * jamais persisté : seul son hash ({@link HashedToken}) est stocké.</p>
 */
public record RawToken(String value) {

    public RawToken {
        Objects.requireNonNull(value, "rawToken must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("rawToken must not be blank");
        }
    }

    /**
     * Crée un raw token à partir d'une valeur brute.
     *
     * <p>Aucune normalisation n'est appliquée : la valeur doit correspondre exactement à celle reçue dans le lien
     * magique, sinon le hash dérivé ne matchera pas le hash persisté.</p>
     *
     * @param value la valeur brute du token
     * @return le raw token validé
     * @throws IllegalArgumentException si la valeur est vide ou ne contient que des blancs
     */
    public static RawToken of(String value) {
        return new RawToken(value);
    }
}
