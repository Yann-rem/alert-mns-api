package com.alertmns.identity.domain.model;

import java.util.Objects;

/**
 * Value Object représentant un mot de passe en clair choisi par l'utilisateur.
 *
 * <p>Le {@code toString()} est masqué pour ne jamais exposer la valeur en clair dans les logs.</p>
 */
public record RawPassword(String value) {

    private static final int MIN_LENGTH = 12;

    public RawPassword {
        Objects.requireNonNull(value, "rawPassword value must not be null");
        if (value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("rawPassword must be at least " + MIN_LENGTH + " characters");
        }
    }

    /**
     * Crée un mot de passe brut à partir d'une valeur en clair.
     *
     * @param value la valeur en clair
     * @return le mot de passe validé
     * @throws IllegalArgumentException si la valeur fait moins de 12 caractères
     */
    public static RawPassword of(String value) {
        return new RawPassword(value);
    }

    @Override
    public String toString() {
        return "RawPassword[REDACTED]";
    }
}
