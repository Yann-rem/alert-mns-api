package com.alertmns.iam.domain.event;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant l'inscription d'un nouvel utilisateur.
 *
 * <p>Le compte est créé avec le statut PENDING, en attente d'activation par un administrateur.</p>
 */
public final class UserRegistered implements DomainEvent {

    private final UserId userId;
    private final Email email;
    private final UserRole role;
    private final OrganisationId organisationId;
    private final Instant occurredOn;

    public UserRegistered(
            UserId userId,
            Email email,
            UserRole role,
            OrganisationId organisationId
    ) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.organisationId = organisationId;
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

    public OrganisationId organisationId() {
        return organisationId;
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
                Objects.equals(organisationId, that.organisationId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, role, organisationId, occurredOn);
    }

    @Override
    public String toString() {
        return "UserRegistered{" +
                "userId=" + userId +
                ", email=" + email +
                ", role=" + role +
                ", organisationId=" + organisationId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
