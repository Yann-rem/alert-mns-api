package com.alertmns.identity.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le prénom d'un utilisateur.
 */
public record FirstName(String value) {

    private static final int MAX_LENGTH = 100;

    public FirstName {
        Objects.requireNonNull(value, "firstName must not be null");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("firstName must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("firstName must not exceed " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Crée un prénom à partir d'une valeur brute.
     *
     * <p>La valeur est normalisée (strip) avant validation.</p>
     *
     * @param value la valeur brute
     * @return le prénom validé
     * @throws IllegalArgumentException si la valeur est vide ou dépasse 100 caractères
     */
    public static FirstName of(String value) {
        return new FirstName(value);
    }
}
