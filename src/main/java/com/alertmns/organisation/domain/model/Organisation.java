package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.OrganisationCreated;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;

/**
 * Agrégat racine représentant une organisation dans le BC Organisation.
 *
 * <p>Une organisation est l'unité de cloisonnement multi-tenant : membres et groupes appartiennent à une seule
 * organisation. Son identifiant {@link OrganisationId} fait partie du <em>shared kernel</em> et est porté par les
 * autres bounded contexts comme simple référence.</p>
 */
public final class Organisation extends AggregateRoot {

    private final OrganisationId id;
    private final OrganisationName name;
    private final Instant createdAt;

    private Organisation(OrganisationId id, OrganisationName name, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Crée une nouvelle organisation.
     *
     * <p>Émet {@link OrganisationCreated}.</p>
     *
     * @param name le nom de l'organisation
     * @param now  instant de l'opération
     * @return la nouvelle organisation créée
     */
    public static Organisation create(OrganisationName name, Instant now) {
        Organisation organisation = new Organisation(
                OrganisationId.generate(),
                name,
                now
        );

        organisation.registerEvent(new OrganisationCreated(organisation.id, now));
        return organisation;
    }

    /**
     * Reconstruit une organisation existante depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     *
     * @return l'organisation reconstituée
     */
    public static Organisation reconstitute(
            OrganisationId id,
            OrganisationName name,
            Instant createdAt
    ) {
        return new Organisation(id, name, createdAt);
    }

    public OrganisationId id() {
        return id;
    }

    public OrganisationName name() {
        return name;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Organisation organisation = (Organisation) o;
        return Objects.equals(id, organisation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Organisation{" +
                "id=" + id +
                ", name=" + name +
                '}';
    }
}
