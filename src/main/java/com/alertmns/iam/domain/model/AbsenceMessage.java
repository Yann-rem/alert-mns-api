package com.alertmns.iam.domain.model;

import java.util.Objects;

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
