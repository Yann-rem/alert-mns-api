package com.alertmns.alerting.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le contenu textuel d'une alerte.
 */
public record AlertContent(String value) {

    private static final int MAX_LENGTH = 4000;

    public AlertContent {
        Objects.requireNonNull(value, "alertContent must not be null");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("alertContent must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("alertContent must not exceed " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Crée un contenu d'alerte à partir d'une valeur brute.
     *
     * @throws IllegalArgumentException si la valeur est vide ou dépasse 4000 caractères
     */
    public static AlertContent of(String value) {
        return new AlertContent(value);
    }
}
