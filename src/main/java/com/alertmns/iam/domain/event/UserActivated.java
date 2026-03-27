package com.alertmns.iam.domain.event;

import com.alertmns.iam.domain.model.UserId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Émis lorsqu'un administrateur active un compte utilisateur.
 * Le compte passe du statut PENDING à ACTIVE.
 */
public final class UserActivated implements DomainEvent {

    private final UserId userId;
    private final Instant occurredOn;

    public UserActivated(UserId userId) {
        this.userId = userId;
        occurredOn = Instant.now();
    }

    public UserId userId() {
        return userId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserActivated that = (UserActivated) o;
        return Objects.equals(userId, that.userId) && Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, occurredOn);
    }

    @Override
    public String toString() {
        return userId + " " + occurredOn;
    }
}
