package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.MemberAddedToGroup;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;

/**
 * Agrégat racine représentant l'appartenance d'un membre à un groupe dans le BC Organisation.
 *
 * <p>Modélise la relation N-N entre {@link Member} et {@link Group}. L'unicité de la paire
 * ({@code memberId}, {@code groupId}) est un invariant <em>set-based</em> garanti au niveau
 * de l'application service et renforcé par une contrainte UNIQUE en base de données.</p>
 */
public final class GroupMembership extends AggregateRoot {

    private final GroupMembershipId id;
    private final OrganisationId organisationId;
    private final GroupId groupId;
    private final MemberId memberId;
    private final Instant joinedAt;

    private GroupMembership(
            GroupMembershipId id,
            OrganisationId organisationId,
            GroupId groupId,
            MemberId memberId,
            Instant joinedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.groupId = Objects.requireNonNull(groupId, "groupId must not be null");
        this.memberId = Objects.requireNonNull(memberId, "memberId must not be null");
        this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt must not be null");
    }

    /**
     * Ajoute un membre à un groupe.
     *
     * <p>L'identifiant et la date d'adhésion sont générés automatiquement.</p>
     *
     * <p>Émet {@link MemberAddedToGroup}.</p>
     *
     * @param organisationId l'identifiant de l'organisation de rattachement
     * @param groupId        l'identifiant du groupe
     * @param memberId       l'identifiant du membre
     * @return la nouvelle appartenance créée
     */
    public static GroupMembership add(OrganisationId organisationId, GroupId groupId, MemberId memberId) {
        GroupMembership groupMembership = new GroupMembership(
                GroupMembershipId.generate(),
                organisationId,
                groupId,
                memberId,
                Instant.now()
        );

        groupMembership.registerEvent(new MemberAddedToGroup(organisationId, groupMembership.id));
        return groupMembership;
    }

    /**
     * Reconstruit une appartenance existante depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     *
     * @return l'appartenance reconstituée
     */
    public static GroupMembership reconstitute(
            GroupMembershipId id,
            OrganisationId organisationId,
            GroupId groupId,
            MemberId memberId,
            Instant joinedAt
    ) {
        return new GroupMembership(id, organisationId, groupId, memberId, joinedAt);
    }

    public GroupMembershipId id() {
        return id;
    }

    public OrganisationId organisationId() {
        return organisationId;
    }

    public GroupId groupId() {
        return groupId;
    }

    public MemberId memberId() {
        return memberId;
    }

    public Instant joinedAt() {
        return joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GroupMembership groupMembership = (GroupMembership) o;
        return Objects.equals(id, groupMembership.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GroupMembership{" +
                "id=" + id +
                ", organisationId=" + organisationId +
                ", groupId=" + groupId +
                ", memberId=" + memberId +
                '}';
    }
}
