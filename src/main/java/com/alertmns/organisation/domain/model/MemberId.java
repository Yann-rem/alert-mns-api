package com.alertmns.organisation.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant unique d'un membre.
 */
public record MemberId(UUID value) {

    public MemberId {
        Objects.requireNonNull(value, "memberId must not be null");
    }

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static MemberId generate() {
        return new MemberId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static MemberId from(UUID value) {
        return new MemberId(value);
    }

    /**
     * Reconstruit un identifiant depuis sa représentation textuelle.
     *
     * @param value la chaîne UUID
     * @return l'identifiant reconstitué
     * @throws IllegalArgumentException si le format UUID est invalide
     */
    public static MemberId from(String value) {
        return new MemberId(UUID.fromString(value));
    }
}
