package com.alertmns.iam.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le nom d'un utilisateur.
 */
public record LastName(String value) {

    private static final int MAX_LENGTH = 100;

    public LastName {
        Objects.requireNonNull(value, "lastName must not be null");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("lastName must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("lastName must not exceed " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Crée un nom à partir d'une valeur brute.
     *
     * <p>La valeur est normalisée (strip) avant validation.</p>
     *
     * @param value la valeur brute
     * @return le nom validé
     * @throws IllegalArgumentException si la valeur est vide ou dépasse 100 caractères
     */
    public static LastName of(String value) {
        return new LastName(value);
    }
}
