package com.alertmns.iam.domain.model;

import java.util.Objects;

/**
 * Prénom d'un utilisateur.
 */
public final class FirstName {

    private static final int MAX_LENGTH = 100;

    private final String value;

    private FirstName(String value) {
        Objects.requireNonNull(value, "firstName must not be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("firstName must not be blank");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("firstName must not exceed " + MAX_LENGTH + " characters");
        }
        this.value = normalized;
    }

    public static FirstName of(String value) {
        return new FirstName(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FirstName that = (FirstName) o;
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
