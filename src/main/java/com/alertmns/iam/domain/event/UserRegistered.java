package com.alertmns.iam.domain.event;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant l'inscription d'un nouvel utilisateur.
 *
 * <p>Le compte est créé avec le statut PENDING, en attente d'activation par un administrateur.</p>
 */
public record UserRegistered(
        UserId userId,
        Email email,
        UserRole role,
        OrganisationId organisationId,
        Instant occurredOn
) implements DomainEvent {

    public UserRegistered(UserId userId, Email email, UserRole role, OrganisationId organisationId) {
        this(userId, email, role, organisationId, Instant.now());
    }
}
