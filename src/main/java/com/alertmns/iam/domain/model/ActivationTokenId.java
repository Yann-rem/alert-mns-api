package com.alertmns.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ActivationTokenId(UUID value) {
    public ActivationTokenId {
        Objects.requireNonNull(value, "activationTokenId must not be null");
    }

    public static ActivationTokenId generate() {
        return new ActivationTokenId(UUID.randomUUID());
    }
}
