package com.mns.alertmns.iam.domain.model;

import java.util.Objects;

/**
 * Nom d'un utilisateur.
 */
public final class LastName {

    private static final int MAX_LENGTH = 100;

    private final String value;

    private LastName(String value) {
        Objects.requireNonNull(value, "Le nom ne peut pas être null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Le nom ne doit pas contenir plus de " + MAX_LENGTH + " caractères");
        }
        this.value = normalized;
    }

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
