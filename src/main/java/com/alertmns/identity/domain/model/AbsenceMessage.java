package com.alertmns.identity.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le message d'absence d'un utilisateur.
 *
 * <p>Immuable : {@link #activate()} et {@link #deactivate()} retournent
 * une nouvelle instance.</p>
 */
public record AbsenceMessage(String content, boolean active) {

    private static final int MAX_LENGTH = 500;

    public AbsenceMessage {
        Objects.requireNonNull(content, "content must not be null");
        content = content.strip();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (content.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("content must not exceed " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Crée un message d'absence.
     *
     * @param content le contenu du message
     * @param active  {@code true} si le message est actif
     * @return le message d'absence créé
     * @throws IllegalArgumentException si le contenu est vide ou dépasse 500 caractères
     */
    public static AbsenceMessage of(String content, boolean active) {
        return new AbsenceMessage(content, active);
    }

    /**
     * Retourne une copie avec le message activé.
     *
     * @return le message d'absence activé
     */
    public AbsenceMessage activate() {
        return new AbsenceMessage(content, true);
    }

    /**
     * Retourne une copie avec le message désactivé.
     *
     * @return le message d'absence désactivé
     */
    public AbsenceMessage deactivate() {
        return new AbsenceMessage(content, false);
    }
}
