package com.alertmns.organisation.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant unique d'un groupe.
 */
public record GroupId(UUID value) {

    public GroupId {
        Objects.requireNonNull(value, "groupId must not be null");
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
}
