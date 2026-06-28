package com.alertmns.messaging.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le contenu textuel d'un message.
 */
public record MessageContent(String value) {

    private static final int MAX_LENGTH = 4000;

    public MessageContent {
        Objects.requireNonNull(value, "messageContent must not be null");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("messageContent must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("messageContent must not exceed " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Crée un contenu de message à partir d'une valeur brute.
     *
     * @throws IllegalArgumentException si la valeur est vide ou dépasse 4000 caractères
     */
    public static MessageContent of(String value) {
        return new MessageContent(value);
    }
}
