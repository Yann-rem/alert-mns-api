package com.alertmns.organisation.domain.event;

import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;

/**
 * Événement de domaine représentant la création d'une organisation.
 *
 * <p>Publié après l'enregistrement d'une nouvelle organisation, il peut être consommé par
 * d'autres bounded contexts pour initialiser leurs propres structures liées à celle-ci.</p>
 */
public record OrganisationCreated(OrganisationId organisationId, Instant occurredOn) implements DomainEvent {

    public OrganisationCreated(OrganisationId organisationId) {
        this(organisationId, Instant.now());
    }
}
