package com.alertmns.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifiant d'un utilisateur.
 */
public final class UserId {

    private final UUID value;

    private UserId(UUID value) {
        Objects.requireNonNull(value, "id must not be null");
        this.value = value;
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId from(UUID value) {
        return new UserId(value);
    }

    public static UserId from(String value) {
        return new UserId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
