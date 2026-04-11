package com.alertmns.organisation.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le nom d'une organisation.
 */
public final class OrganisationName {

    private static final int MAX_LENGTH = 150;

    private final String value;

    private OrganisationName(String value) {
        Objects.requireNonNull(value, "organisationName must not be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("organisationName must not be blank");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "organisationName must not exceed " + MAX_LENGTH + " characters"
            );
        }
        this.value = normalized;
    }

    /**
     * Crée un nom d'organisation à partir d'une valeur brute.
     *
     * <p>La valeur est normalisée (trim) avant validation.</p>
     *
     * @param value la valeur brute
     * @return le nom validé
     * @throws IllegalArgumentException si la valeur est vide ou dépasse 150 caractères
     */
    public static OrganisationName of(String value) {
        return new OrganisationName(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrganisationName that = (OrganisationName) o;
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
