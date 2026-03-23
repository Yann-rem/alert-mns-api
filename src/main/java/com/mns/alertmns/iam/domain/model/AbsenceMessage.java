package com.mns.alertmns.iam.domain.model;

import java.util.Objects;

public final class AbsenceMessage {

    private final String content;
    private final boolean active;

    private AbsenceMessage(String content, boolean active) {
        Objects.requireNonNull(content, "Le contenu du message d'absence ne peut pas être null");
        if (content.isBlank()) {
            throw new IllegalArgumentException("Le contenu du message d'absence ne doit pas être vide");
        }
        this.content = content;
        this.active = active;
    }

    public static AbsenceMessage of(String content, boolean active) {
        return new AbsenceMessage(content, active);
    }

    public AbsenceMessage activate() {
        return new AbsenceMessage(content, true);
    }

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
