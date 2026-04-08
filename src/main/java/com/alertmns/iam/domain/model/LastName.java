package com.alertmns.iam.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le nom d'un utilisateur.
 */
public final class LastName {

    private static final int MAX_LENGTH = 100;

    private final String value;

    private LastName(String value) {
        Objects.requireNonNull(value, "lastName must not be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("lastName must not be blank");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("lastName must not exceed " + MAX_LENGTH + " characters");
        }
        this.value = normalized;
    }

    /**
     * Crée un nom à partir d'une valeur brute.
     *
     * <p>La valeur est normalisée (trim) avant validation.</p>
     *
     * @param value la valeur brute
     * @return le nom validé
     * @throws IllegalArgumentException si la valeur est vide ou dépasse 100 caractères
     */
    public static LastName of(String value) {
        return new LastName(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LastName that = (LastName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
