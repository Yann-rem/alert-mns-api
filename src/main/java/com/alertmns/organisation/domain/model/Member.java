package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.MemberJoined;
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
 * <p>Un membre matérialise l'appartenance d'un utilisateur (référencé par son {@code userId}) à une organisation. Un
 * membre naît directement en état {@link MemberStatus#ACTIVE} via {@link #createActive}, puis son état transite via
 * {@link #suspend} et {@link #reactivate}.</p>
 *
 * <p>Le {@code userId} est un simple {@link UUID} — aucun couplage vers le BC Identity n'est introduit dans le domaine
 * Organisation.</p>
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
     * Crée un nouveau membre directement en état {@link MemberStatus#ACTIVE}.
     *
     * <p>Émet {@link MemberJoined}.</p>
     *
     * @param organisationId organisation rejointe
     * @param userId         utilisateur qui rejoint
     * @param role           rôle attribué
     * @return le nouveau membre, en statut ACTIVE
     */
    public static Member createActive(
            OrganisationId organisationId,
            UUID userId,
            MemberRole role
    ) {
        Member member = new Member(
                MemberId.generate(),
                organisationId,
                userId,
                role,
                MemberStatus.ACTIVE,
                Instant.now()
        );
        member.registerEvent(new MemberJoined(organisationId, member.id, userId, role));
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
