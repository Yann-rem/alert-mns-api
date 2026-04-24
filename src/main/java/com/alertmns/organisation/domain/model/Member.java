package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.MemberActivated;
import com.alertmns.organisation.domain.event.MemberInvited;
import com.alertmns.organisation.domain.event.MemberReactivated;
import com.alertmns.organisation.domain.event.MemberSuspended;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrégat racine représentant un membre d'une organisation dans le BC Organisation.
 *
 * <p>Un membre matérialise l'appartenance d'un utilisateur (référencé par son {@code userId}
 * opaque, issu du BC IAM) à une organisation. Il possède son propre cycle de vie
 * (PENDING → ACTIVE → SUSPENDED → ACTIVE) indépendant de celui du compte utilisateur.</p>
 *
 * <p>Le {@code userId} est un simple {@link java.util.UUID} — aucun couplage vers le BC IAM
 * n'est introduit dans le domaine Organisation.</p>
 */
public final class Member extends AggregateRoot {

    private final MemberId id;
    private final OrganisationId organisationId;
    private final UUID userId;
    private final MemberRole role;
    private MemberStatus status;
    private final Instant joinedAt;

    private Member(
            MemberId id,
            OrganisationId organisationId,
            UUID userId,
            MemberRole role,
            MemberStatus status,
            Instant joinedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt must not be null");
    }

    /**
     * Invite un nouveau membre dans une organisation avec le statut {@link MemberStatus#PENDING}.
     *
     * <p>L'identifiant et la date d'adhésion sont générés automatiquement.</p>
     *
     * <p>Émet {@link MemberInvited}.</p>
     *
     * @param organisationId l'identifiant de l'organisation de rattachement
     * @param userId         l'identifiant du compte utilisateur (référence cross-BC)
     * @param role           le rôle attribué au sein de l'organisation
     * @return le nouveau membre créé
     */
    public static Member invite(
            OrganisationId organisationId,
            UUID userId,
            MemberRole role
    ) {
        Member member = new Member(
                MemberId.generate(),
                organisationId,
                userId,
                role,
                MemberStatus.PENDING,
                Instant.now()
        );

        member.registerEvent(new MemberInvited(member.organisationId, member.id));
        return member;
    }

    /**
     * Reconstruit un membre existant depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     *
     * @return le membre reconstitué
     */
    public static Member reconstitute(
            MemberId id,
            OrganisationId organisationId,
            UUID userId,
            MemberRole role,
            MemberStatus status,
            Instant joinedAt
    ) {
        return new Member(
                id,
                organisationId,
                userId,
                role,
                status,
                joinedAt
        );
    }

    /**
     * Active un membre en attente (PENDING → ACTIVE).
     *
     * <p>Émet {@link MemberActivated}.</p>
     *
     * @throws IllegalStateException si le statut n'est pas {@link MemberStatus#PENDING}
     */
    public void activate() {
        requireStatus(MemberStatus.PENDING, "activate");
        status = MemberStatus.ACTIVE;
        registerEvent(new MemberActivated(organisationId, id));
    }

    /**
     * Réactive un membre suspendu (SUSPENDED → ACTIVE).
     *
     * <p>Émet {@link MemberReactivated}.</p>
     *
     * @throws IllegalStateException si le statut n'est pas {@link MemberStatus#SUSPENDED}
     */
    public void reactivate() {
        requireStatus(MemberStatus.SUSPENDED, "reactivate");
        status = MemberStatus.ACTIVE;
        registerEvent(new MemberReactivated(organisationId, id));
    }

    /**
     * Suspend un membre actif (ACTIVE → SUSPENDED).
     *
     * <p>Émet {@link MemberSuspended}.</p>
     *
     * @throws IllegalStateException si le statut n'est pas {@link MemberStatus#ACTIVE}
     */
    public void suspend() {
        requireStatus(MemberStatus.ACTIVE, "suspend");
        status = MemberStatus.SUSPENDED;
        registerEvent(new MemberSuspended(organisationId, id));
    }

    private void requireStatus(MemberStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Cannot " + action + ": member is not " + expected + ". Current status: " + status
            );
        }
    }

    public MemberId id() {
        return id;
    }

    public OrganisationId organisationId() {
        return organisationId;
    }

    public UUID userId() {
        return userId;
    }

    public MemberRole role() {
        return role;
    }

    public MemberStatus status() {
        return status;
    }

    public Instant joinedAt() {
        return joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", organisationId=" + organisationId +
                ", userId=" + userId +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}
