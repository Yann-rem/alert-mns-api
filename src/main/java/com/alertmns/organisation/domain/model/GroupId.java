package com.alertmns.organisation.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant unique d'un groupe.
 */
public final class GroupId {

    private final UUID value;

    private GroupId(UUID value) {
        Objects.requireNonNull(value, "id must not be null");
        this.value = value;
    }

    public static GroupId generate() {
        return new GroupId(UUID.randomUUID());
    }

    public static GroupId from(UUID value) {
        return new GroupId(value);
    }

    public static GroupId from(String value) {
        return new GroupId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GroupId groupId = (GroupId) o;
        return Objects.equals(value, groupId.value);
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
