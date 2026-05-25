package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.OrganisationCreated;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;

/**
 * Agrégat racine représentant une organisation dans le BC Organisation.
 *
 * <p>Une organisation est l'unité de cloisonnement multi-tenant : membres et groupes
 * appartiennent à une seule organisation. Son identifiant {@link OrganisationId} fait
 * partie du <em>shared kernel</em> et est porté par les autres bounded contexts (notamment
 * Identity) comme simple référence, sans clé étrangère physique.</p>
 *
 * <p>L'unicité du nom ({@link OrganisationName}) est un invariant <em>set-based</em> qui ne peut
 * pas être validé par l'agrégat seul : il est garanti au niveau de l'application service (via
 * une vérification au repository) et renforcé par une contrainte UNIQUE en base de données.</p>
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
     * <p>L'identifiant et la date de création sont générés automatiquement.
     * L'unicité du nom n'est pas vérifiée à ce niveau : elle relève de l'application
     * service, qui doit interroger le repository avant d'appeler cette factory.</p>
     *
     * <p>Émet {@link OrganisationCreated}.</p>
     *
     * @param name le nom de l'organisation
     * @return la nouvelle organisation créée
     */
    public static Organisation create(OrganisationName name) {
        Organisation organisation = new Organisation(
                OrganisationId.generate(),
                name,
                Instant.now()
        );

        organisation.registerEvent(new OrganisationCreated(organisation.id));
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
