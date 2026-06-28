package com.alertmns.messaging.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant unique d'un message.
 */
public record MessageId(UUID value) {

    public MessageId {
        Objects.requireNonNull(value, "messageId must not be null");
    }

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static MessageId generate() {
        return new MessageId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static MessageId from(UUID value) {
        return new MessageId(value);
    }

    /**
     * Reconstruit un identifiant depuis sa représentation textuelle.
     *
     * @param value la chaîne UUID
     * @return l'identifiant reconstitué
     * @throws IllegalArgumentException si le format UUID est invalide
     */
    public static MessageId from(String value) {
        return new MessageId(UUID.fromString(value));
    }
}
