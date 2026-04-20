package com.alertmns.organisation.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant unique d'une appartenance membre-groupe.
 */
public record GroupMembershipId(UUID value) {

    public GroupMembershipId {
        Objects.requireNonNull(value, "groupMembershipId must not be null");
    }

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static GroupMembershipId generate() {
        return new GroupMembershipId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static GroupMembershipId from(UUID value) {
        return new GroupMembershipId(value);
    }

    /**
     * Reconstruit un identifiant depuis sa représentation textuelle.
     *
     * @param value la chaîne UUID
     * @return l'identifiant reconstitué
     * @throws IllegalArgumentException si le format UUID est invalide
     */
    public static GroupMembershipId from(String value) {
        return new GroupMembershipId(UUID.fromString(value));
    }
}
