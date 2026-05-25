package com.alertmns.identity.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant d'un token d'activation.
 */
public record ActivationTokenId(UUID value) {

    public ActivationTokenId {
        Objects.requireNonNull(value, "activationTokenId must not be null");
    }

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static ActivationTokenId generate() {
        return new ActivationTokenId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static ActivationTokenId from(UUID value) {
        return new ActivationTokenId(value);
    }
}
