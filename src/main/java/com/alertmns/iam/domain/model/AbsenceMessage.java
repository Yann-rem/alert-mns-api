package com.alertmns.iam.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le message d'absence d'un utilisateur.
 *
 * <p>Immuable : {@link #activate()} et {@link #deactivate()} retournent
 * une nouvelle instance.</p>
 */
public final class AbsenceMessage {

    private static final int MAX_LENGTH = 500;

    private final String content;
    private final boolean active;

    private AbsenceMessage(String content, boolean active) {
        Objects.requireNonNull(content, "content must not be null");
        String normalized = content.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("content must not exceed " + MAX_LENGTH + " characters");
        }
        this.content = normalized;
        this.active = active;
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

    public String content() {
        return content;
    }

    public boolean active() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AbsenceMessage that = (AbsenceMessage) o;
        return Objects.equals(content, that.content) && active == that.active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, active);
    }

    @Override
    public String toString() {
        return content;
    }
}
