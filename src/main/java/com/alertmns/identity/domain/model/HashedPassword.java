package com.alertmns.identity.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le mot de passe haché d'un utilisateur.
 */
public record HashedPassword(String value) {

    private static final int MAX_LENGTH = 255;
    private static final String UNSET_SENTINEL = "$unset$";

    public HashedPassword {
        Objects.requireNonNull(value, "hashedPassword must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("hashedPassword must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("hashedPassword must not exceed " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Crée un mot de passe haché à partir d'une valeur brute.
     *
     * @param value le hash du mot de passe
     * @return le mot de passe haché validé
     * @throws IllegalArgumentException si la valeur est vide ou dépasse 255 caractères
     */
    public static HashedPassword of(String value) {
        return new HashedPassword(value);
    }

    /**
     * Retourne la sentinelle « non définie » pour un User créé en {@code PENDING} via une invitation. Cette valeur
     * n'est pas un hash bcrypt valide et ne matchera jamais un mot de passe utilisateur. Elle sera remplacée lors du
     * redeem du magic-link via {@code User.activateWithPassword(...)}.
     */
    public static HashedPassword unset() {
        return new HashedPassword(UNSET_SENTINEL);
    }

    /**
     * @return {@code true} si ce hash est la sentinelle « non définie » (User PENDING jamais activé).
     */
    public boolean isUnset() {
        return UNSET_SENTINEL.equals(value);
    }
}
