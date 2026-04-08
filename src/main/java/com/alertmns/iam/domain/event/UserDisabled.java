package com.alertmns.iam.domain.event;

import com.alertmns.iam.domain.model.UserId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant la désactivation d'un compte utilisateur.
 *
 * <p>Le compte passe du statut ACTIVE à DISABLED.</p>
 */
public final class UserDisabled implements DomainEvent {

    private final UserId userId;
    private final Instant occurredOn;

    public UserDisabled(UserId userId) {
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
        UserDisabled that = (UserDisabled) o;
        return Objects.equals(userId, that.userId) && Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, occurredOn);
    }

    @Override
    public String toString() {
        return "UserDisabled{" +
                "userId=" + userId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
