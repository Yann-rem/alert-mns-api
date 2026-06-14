package com.alertmns.messaging.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le nom d'une conversation.
 */
public record ConversationName(String value) {

    private static final int MAX_LENGTH = 150;

    public ConversationName {
        Objects.requireNonNull(value, "conversationName must not be null");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("conversationName must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("conversationName must not exceed " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Crée un nom de conversation à partir d'une valeur brute.
     *
     * <p>La valeur est normalisée (strip) avant validation.</p>
     *
     * @param value la valeur brute
     * @return le nom validé
     * @throws IllegalArgumentException si la valeur est vide ou dépasse 150 caractères
     */
    public static ConversationName of(String value) {
        return new ConversationName(value);
    }
}
