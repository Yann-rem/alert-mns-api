package com.alertmns.organisation.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le nom d'un groupe au sein d'une organisation.
 */
public final class GroupName {

    private static final int MAX_LENGTH = 150;

    private final String value;

    private GroupName(String value) {
        Objects.requireNonNull(value, "groupName must not be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("groupName must not be blank");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "groupName must not exceed " + MAX_LENGTH + " characters"
            );
        }
        this.value = normalized;
    }

    public static GroupName of(String value) {
        return new GroupName(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GroupName groupName = (GroupName) o;
        return Objects.equals(value, groupName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
