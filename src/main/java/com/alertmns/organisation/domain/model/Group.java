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
 * <p>Un groupe appartient à une seule organisation et permet de regrouper des membres via {@code GroupMembership}.
 * L'unicité du nom par organisation est un invariant garanti au niveau de l'application service.</p>
 *
 * <p>Son {@link GroupKind} distingue le canal {@link GroupKind#GENERAL} unique par organisation, provisionné au
 * bootstrap, des groupes {@link GroupKind#STANDARD} créés à la demande. Le {@code kind} est fixé à la création et
 * n'est jamais modifié.</p>
 */
public final class Group extends AggregateRoot {

    private final GroupId id;
    private final OrganisationId organisationId;
    private GroupName name;
    private final GroupKind kind;
    private final Instant createdAt;

    private Group(GroupId id, OrganisationId organisationId, GroupName name, GroupKind kind, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Crée un groupe {@link GroupKind#STANDARD}
     *
     * <p>Émet {@link GroupCreated}.</p>
     *
     * @param organisationId l'identifiant de l'organisation de rattachement
     * @param name           le nom du groupe
     * @param now            instant de l'opération
     * @return le nouveau groupe créé, de kind STANDARD
     */
    public static Group createStandard(OrganisationId organisationId, GroupName name, Instant now) {
        Group group = new Group(GroupId.generate(), organisationId, name, GroupKind.STANDARD, now);

        group.registerEvent(new GroupCreated(group.organisationId, group.id, group.name, group.kind, now));
        return group;
    }

    /**
     * Crée le groupe {@link GroupKind#GENERAL} d'une organisation.
     *
     * <p>Émet {@link GroupCreated}.</p>
     *
     * @param organisationId l'identifiant de l'organisation de rattachement
     * @param name           le nom du canal général
     * @param now            instant de l'opération
     * @return le nouveau groupe créé, de kind GENERAL
     */
    public static Group createGeneral(OrganisationId organisationId, GroupName name, Instant now) {
        Group group = new Group(GroupId.generate(), organisationId, name, GroupKind.GENERAL, now);

        group.registerEvent(new GroupCreated(group.organisationId, group.id, group.name, group.kind, now));
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
            GroupKind kind,
            Instant createdAt
    ) {
        return new Group(id, organisationId, name, kind, createdAt);
    }

    /**
     * Renomme le groupe.
     *
     * @param name le nouveau nom du groupe
     * @param now  instant de l'opération
     */
    public void rename(GroupName name, Instant now) {
        Objects.requireNonNull(name, "name must not be null");
        if (this.name.equals(name)) {
            return;
        }
        this.name = name;
        registerEvent(new GroupRenamed(organisationId, id, name, now));
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

    public GroupKind kind() {
        return kind;
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
                ", organisationId=" + organisationId +
                ", name=" + name +
                ", kind=" + kind +
                '}';
    }
}
