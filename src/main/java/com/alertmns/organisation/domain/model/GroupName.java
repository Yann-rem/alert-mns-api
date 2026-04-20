package com.alertmns.organisation.domain.model;

import java.util.Objects;

/**
 * Value Object représentant le nom d'un groupe au sein d'une organisation.
 */
public record GroupName(String value) {

    private static final int MAX_LENGTH = 150;

    public GroupName {
        Objects.requireNonNull(value, "groupName must not be null");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("groupName must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "groupName must not exceed " + MAX_LENGTH + " characters"
            );
        }
    }

    public static GroupName of(String value) {
        return new GroupName(value);
    }
}
