package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.MembershipInvitationAccepted;
import com.alertmns.organisation.domain.event.MembershipInvitationIssued;
import com.alertmns.organisation.domain.exception.InvitationExpiredException;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.Email;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrégat racine représentant une invitation à rejoindre une organisation.
 *
 * <p>Capture l'état intermédiaire <em>« invité mais pas encore membre »</em> : un admin émet une invitation pour un
 * email + rôle ; l'invité accepte en activant son compte via le magic-link. À l'acceptation, un {@code Member} ACTIVE
 * est créé pour le couple {@code (organisationId, userId, role)}.</p>
 *
 * <p>L'agrégat ne porte <strong>pas</strong> de {@code userId} : il est identifié par l'email invité, qui est la clé
 * fonctionnelle de l'invitation. Le lien avec un User concret est établi à l'acceptation, via
 * {@link #accept(Instant, UUID)}.</p>
 */
public final class MembershipInvitation extends AggregateRoot {

    private final MembershipInvitationId id;
    private final OrganisationId organisationId;
    private final Email invitedEmail;
    private final MemberRole role;
    private MembershipInvitationStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;

    private MembershipInvitation(
            MembershipInvitationId id,
            OrganisationId organisationId,
            Email invitedEmail,
            MemberRole role,
            MembershipInvitationStatus status,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.invitedEmail = Objects.requireNonNull(invitedEmail, "invitedEmail must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    /**
     * Émet une nouvelle invitation en statut {@link MembershipInvitationStatus#PENDING}.
     *
     * <p>L'identifiant et les timestamps sont générés automatiquement.
     * {@code expiresAt = createdAt + ttl}.</p>
     *
     * <p>Émet {@link MembershipInvitationIssued}.</p>
     *
     * @param organisationId organisation cible
     * @param invitedEmail   email de l'invité (clé fonctionnelle)
     * @param role           rôle qui sera accordé à l'acceptation
     * @param ttl            durée de validité de l'invitation
     * @return la nouvelle invitation créée
     */
    public static MembershipInvitation issue(
            OrganisationId organisationId,
            Email invitedEmail,
            MemberRole role,
            Duration ttl
    ) {
        Objects.requireNonNull(ttl, "ttl must not be null");
        Instant now = Instant.now();
        MembershipInvitation invitation = new MembershipInvitation(
                MembershipInvitationId.generate(),
                organisationId,
                invitedEmail,
                role,
                MembershipInvitationStatus.PENDING,
                now,
                now.plus(ttl)
        );

        invitation.registerEvent(new MembershipInvitationIssued(invitation.id, organisationId, invitedEmail, role));
        return invitation;
    }

    /**
     * Reconstruit une invitation existante depuis la persistance.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     */
    public static MembershipInvitation reconstitute(
            MembershipInvitationId id,
            OrganisationId organisationId,
            Email invitedEmail,
            MemberRole role,
            MembershipInvitationStatus status,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new MembershipInvitation(
                id, organisationId, invitedEmail, role, status, createdAt, expiresAt);
    }

    /**
     * Accepte l'invitation et transitionne {@code PENDING → ACCEPTED}.
     *
     * <p>Émet {@link MembershipInvitationAccepted}.</p>
     *
     * @param now    instant courant
     * @param userId identifiant de l'utilisateur qui accepte
     * @throws IllegalStateException      si le statut n'est pas {@link MembershipInvitationStatus#PENDING}
     * @throws InvitationExpiredException si {@code now > expiresAt}
     * @throws NullPointerException       si {@code now} ou {@code userId} est null
     */
    public void accept(Instant now, UUID userId) {
        Objects.requireNonNull(now, "now must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        if (status != MembershipInvitationStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot accept: invitation is not PENDING. Current status: " + status);
        }
        if (now.isAfter(expiresAt)) {
            throw new InvitationExpiredException(id);
        }
        this.status = MembershipInvitationStatus.ACCEPTED;
        registerEvent(new MembershipInvitationAccepted(id, organisationId, userId, role));
    }

    public MembershipInvitationId id() {
        return id;
    }

    public OrganisationId organisationId() {
        return organisationId;
    }

    public Email invitedEmail() {
        return invitedEmail;
    }

    public MemberRole role() {
        return role;
    }

    public MembershipInvitationStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MembershipInvitation that = (MembershipInvitation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MembershipInvitation{" +
                "id=" + id +
                ", organisationId=" + organisationId +
                ", invitedEmail=" + invitedEmail +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}
