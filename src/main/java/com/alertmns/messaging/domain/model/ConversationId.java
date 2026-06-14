package com.alertmns.messaging.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant l'identifiant unique d'une conversation.
 */
public record ConversationId(UUID value) {

    public ConversationId {
        Objects.requireNonNull(value, "conversationId must not be null");
    }

    /**
     * Génère un nouvel identifiant aléatoire.
     *
     * @return un nouvel identifiant
     */
    public static ConversationId generate() {
        return new ConversationId(UUID.randomUUID());
    }

    /**
     * Reconstruit un identifiant depuis un UUID existant.
     *
     * @param value l'UUID source
     * @return l'identifiant reconstitué
     */
    public static ConversationId from(UUID value) {
        return new ConversationId(value);
    }

    /**
     * Reconstruit un identifiant depuis sa représentation textuelle.
     *
     * @param value la chaîne UUID
     * @return l'identifiant reconstitué
     * @throws IllegalArgumentException si le format UUID est invalide
     */
    public static ConversationId from(String value) {
        return new ConversationId(UUID.fromString(value));
    }
}
