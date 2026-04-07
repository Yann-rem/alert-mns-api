package com.alertmns.iam.domain.event;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Émis lorsqu'un nouvel utilisateur s'inscrit avec succès.
 * Le compte est créé avec le statut PENDING, en attente d'activation par un administrateur.
 */
public final class UserRegistered implements DomainEvent {

    private final UserId userId;
    private final Email email;
    private final UserRole role;
    private final Instant occurredOn;

    public UserRegistered(UserId userId, Email email, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        occurredOn = Instant.now();
    }

    public UserId userId() {
        return userId;
    }

    public Email email() {
        return email;
    }

    public UserRole role() {
        return role;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserRegistered that = (UserRegistered) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(email, that.email) &&
                role == that.role &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, role, occurredOn);
    }

    @Override
    public String toString() {
        return "UserRegistered{" +
                "userId=" + userId +
                ", email=" + email +
                ", role=" + role +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
