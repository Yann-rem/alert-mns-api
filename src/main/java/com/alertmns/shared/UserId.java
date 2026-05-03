package com.alertmns.shared;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant d'un utilisateur.
 */
public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "userId must not be null");
    }

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static UserId from(UUID value) {
        return new UserId(value);
    }

    /**
     * Reconstruit un identifiant depuis sa représentation textuelle.
     *
     * @param value la chaîne UUID
     * @return l'identifiant reconstitué
     * @throws IllegalArgumentException si le format UUID est invalide
     */
    public static UserId from(String value) {
        return new UserId(UUID.fromString(value));
    }
}
