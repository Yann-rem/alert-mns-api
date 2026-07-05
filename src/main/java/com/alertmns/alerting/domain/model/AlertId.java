package com.alertmns.alerting.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant unique d'une alerte.
 */
public record AlertId(UUID value) {

    public AlertId {
        Objects.requireNonNull(value, "alertId must not be null");
    }

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static AlertId generate() {
        return new AlertId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static AlertId from(UUID value) {
        return new AlertId(value);
    }

    /**
     * Reconstruit un identifiant depuis sa représentation textuelle.
     *
     * @param value la chaîne UUID
     * @return l'identifiant reconstitué
     * @throws IllegalArgumentException si le format UUID est invalide
     */
    public static AlertId from(String value) {
        return new AlertId(UUID.fromString(value));
    }
}
