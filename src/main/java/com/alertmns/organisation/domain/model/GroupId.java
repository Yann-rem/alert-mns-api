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

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static GroupId generate() {
        return new GroupId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static GroupId from(UUID value) {
        return new GroupId(value);
    }

    /**
     * Reconstruit un identifiant depuis sa représentation textuelle.
     *
     * @param value la chaîne UUID
     * @return l'identifiant reconstitué
     * @throws IllegalArgumentException si le format UUID est invalide
     */
    public static GroupId from(String value) {
        return new GroupId(UUID.fromString(value));
    }
}
