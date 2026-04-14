package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.MemberAddedToGroup;
import com.alertmns.shared.AggregateRoot;

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
    private final MemberId memberId;
    private final GroupId groupId;
    private final Instant joinedAt;

    private GroupMembership(
            GroupMembershipId id,
            MemberId memberId,
            GroupId groupId,
            Instant joinedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.memberId = Objects.requireNonNull(memberId, "memberId must not be null");
        this.groupId = Objects.requireNonNull(groupId, "groupId must not be null");
        this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt must not be null");
    }

    /**
     * Ajoute un membre à un groupe.
     *
     * <p>L'identifiant et la date d'adhésion sont générés automatiquement.</p>
     *
     * <p>Émet {@link MemberAddedToGroup}.</p>
     *
     * @param memberId l'identifiant du membre
     * @param groupId  l'identifiant du groupe
     * @return la nouvelle appartenance créée
     */
    public static GroupMembership add(MemberId memberId, GroupId groupId) {
        GroupMembership groupMembership = new GroupMembership(
                GroupMembershipId.generate(),
                memberId,
                groupId,
                Instant.now()
        );

        groupMembership.registerEvent(new MemberAddedToGroup(groupMembership.id));
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
            MemberId memberId,
            GroupId groupId,
            Instant joinedAt
    ) {
        return new GroupMembership(id, memberId, groupId, joinedAt);
    }

    public GroupMembershipId id() {
        return id;
    }

    public MemberId memberId() {
        return memberId;
    }

    public GroupId groupId() {
        return groupId;
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
                ", memberId=" + memberId +
                ", groupId=" + groupId +
                '}';
    }
}
