package com.alertmns.iam.domain.model;

import java.util.Objects;

/**
 * Mot de passe haché d'un utilisateur.
 */
public final class HashedPassword {

    private static final int MAX_LENGTH = 255;
    private final String value;

    private HashedPassword(String value) {
        Objects.requireNonNull(value, "hashedPassword must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("hashedPassword must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("hashedPassword must not exceed " + MAX_LENGTH + " characters");
        }
        this.value = value;
    }

    public static HashedPassword of(String value) {
        return new HashedPassword(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HashedPassword hashedPassword = (HashedPassword) o;
        return Objects.equals(value, hashedPassword.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "****";
    }
}
