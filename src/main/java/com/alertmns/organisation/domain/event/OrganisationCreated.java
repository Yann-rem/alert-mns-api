package com.alertmns.organisation.domain.event;

import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant la création d'une organisation.
 *
 * <p>Publié après l'enregistrement d'une nouvelle organisation, il peut être consommé par
 * d'autres bounded contexts pour initialiser leurs propres structures liées à celle-ci.</p>
 */
public final class OrganisationCreated implements DomainEvent {

    private final OrganisationId organisationId;
    private final Instant occurredOn;

    public OrganisationCreated(OrganisationId organisationId) {
        this.organisationId = organisationId;
        occurredOn = Instant.now();
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
        OrganisationCreated that = (OrganisationCreated) o;
        return Objects.equals(organisationId, that.organisationId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organisationId, occurredOn);
    }

    @Override
    public String toString() {
        return "OrganisationCreated{" +
                "organisationId=" + organisationId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
