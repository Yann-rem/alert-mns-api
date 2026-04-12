package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.OrganisationName;
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
    private final OrganisationName name;
    private final Instant occurredOn;

    public OrganisationCreated(OrganisationId organisationId, OrganisationName name) {
        this.organisationId = organisationId;
        this.name = name;
        occurredOn = Instant.now();
    }

    public OrganisationId organisationId() {
        return organisationId;
    }

    public OrganisationName name() {
        return name;
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
                Objects.equals(name, that.name) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organisationId, name, occurredOn);
    }

    @Override
    public String toString() {
        return "OrganisationCreated{" +
                "organisationId=" + organisationId +
                ", name=" + name +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
