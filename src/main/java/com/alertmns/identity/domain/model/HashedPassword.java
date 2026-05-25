package com.alertmns.identity.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le mot de passe haché d'un utilisateur.
 */
public record HashedPassword(String value) {

    private static final int MAX_LENGTH = 255;

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
}
