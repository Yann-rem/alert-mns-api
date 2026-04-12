package com.alertmns.iam.domain.event;

import com.alertmns.iam.domain.model.UserId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant la réactivation d'un compte utilisateur.
 *
 * <p>Le compte passe du statut DISABLED à ACTIVE.</p>
 */
public final class UserReactivated implements DomainEvent {

    private final UserId userId;
    private final Instant occurredOn;

    public UserReactivated(UserId userId) {
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
        UserReactivated that = (UserReactivated) o;
        return Objects.equals(userId, that.userId) && Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, occurredOn);
    }

    @Override
    public String toString() {
        return "UserReactivated{" +
                "userId=" + userId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
