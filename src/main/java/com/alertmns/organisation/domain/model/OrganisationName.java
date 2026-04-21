package com.alertmns.organisation.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le nom d'une organisation.
 */
public record OrganisationName(String value) {

    private static final int MAX_LENGTH = 150;

    public OrganisationName {
        Objects.requireNonNull(value, "organisationName must not be null");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("organisationName must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("organisationName must not exceed " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Crée un nom d'organisation à partir d'une valeur brute.
     *
     * <p>La valeur est normalisée (strip) avant validation.</p>
     *
     * @param value la valeur brute
     * @return le nom validé
     * @throws IllegalArgumentException si la valeur est vide ou dépasse 150 caractères
     */
    public static OrganisationName of(String value) {
        return new OrganisationName(value);
    }
}
