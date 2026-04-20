package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.GroupCreated;
import com.alertmns.organisation.domain.event.GroupRenamed;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;

/**
 * Agrégat racine représentant un groupe dans le BC Organisation.
 *
 * <p>Un groupe appartient à une seule organisation et permet de regrouper des membres
 * (via {@code GroupMembership}). L'unicité du nom par organisation est un invariant
 * <em>set-based</em> garanti au niveau de l'application service.</p>
 */
public final class Group extends AggregateRoot {

    private final GroupId id;
    private final OrganisationId organisationId;
    private GroupName name;
    private final Instant createdAt;

    private Group(GroupId id, OrganisationId organisationId, GroupName name, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Crée un nouveau groupe dans une organisation.
     *
     * <p>L'identifiant et la date de création sont générés automatiquement.</p>
     *
     * <p>Émet {@link GroupCreated}.</p>
     *
     * @param name           le nom du groupe
     * @param organisationId l'identifiant de l'organisation de rattachement
     * @return le nouveau groupe créé
     */
    public static Group create(OrganisationId organisationId, GroupName name) {
        Group group = new Group(GroupId.generate(), organisationId, name, Instant.now());
        group.registerEvent(new GroupCreated(group.organisationId, group.id, group.name));
        return group;
    }

    /**
     * Reconstruit un groupe existant depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     *
     * @return le groupe reconstitué
     */
    public static Group reconstitute(
            GroupId id,
            OrganisationId organisationId,
            GroupName name,
            Instant createdAt
    ) {
        return new Group(id, organisationId, name, createdAt);
    }

    /**
     * Renomme le groupe.
     *
     * <p>Émet {@link GroupRenamed}.</p>
     *
     * @param name le nouveau nom du groupe
     */
    public void rename(GroupName name) {
        Objects.requireNonNull(name, "name must not be null");
        if (this.name.equals(name)) {
            return;
        }
        this.name = name;
        registerEvent(new GroupRenamed(this.id, name));
    }

    public GroupId id() {
        return id;
    }

    public OrganisationId organisationId() {
        return organisationId;
    }

    public GroupName name() {
        return name;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name=" + name +
                ", organisationId=" + organisationId +
                '}';
    }
}
