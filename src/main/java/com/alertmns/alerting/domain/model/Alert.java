package com.alertmns.alerting.domain.model;

import com.alertmns.alerting.domain.event.AlertBroadcast;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrégat racine représentant une alerte diffusée dans le BC Alerting.
 *
 * <p>Une alerte est une annonce one-to-many, émise par un membre autorisé (l'issuer, de rôle ADMIN ou MANAGER) vers
 * une audience — toute l'organisation ou un groupe précis (cf. {@link AlertAudience}). Elle est diffusée
 * immédiatement à sa création via {@link #broadcast} : il n'y a ni brouillon ni planification au MVP. Une fois
 * diffusée, l'alerte est immuable.</p>
 *
 * <p>Événement : {@link AlertBroadcast}.</p>
 */
public final class Alert extends AggregateRoot {

    private final AlertId id;
    private final OrganisationId organisationId;
    private final UUID issuerId;
    private final AlertContent content;
    private final AlertAudience audience;
    private final AlertLevel level;
    private final Instant issuedAt;

    private Alert(
            AlertId id,
            OrganisationId organisationId,
            UUID issuerId,
            AlertContent content,
            AlertAudience audience,
            AlertLevel level,
            Instant issuedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.issuerId = Objects.requireNonNull(issuerId, "issuerId must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.audience = Objects.requireNonNull(audience, "audience must not be null");
        this.level = Objects.requireNonNull(level, "level must not be null");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt must not be null");
    }

    /**
     * Diffuse une nouvelle alerte.
     *
     * <p>Génère l'identifiant en interne et émet {@link AlertBroadcast}.</p>
     *
     * @param organisationId organisation de rattachement
     * @param issuerId       membre émetteur (ADMIN ou MANAGER)
     * @param content        contenu de l'alerte
     * @param audience       cible de l'alerte
     * @param level          niveau de gravité
     * @param now            instant de diffusion
     * @return la nouvelle alerte diffusée
     */
    public static Alert broadcast(
            OrganisationId organisationId,
            UUID issuerId,
            AlertContent content,
            AlertAudience audience,
            AlertLevel level,
            Instant now
    ) {
        Alert alert = new Alert(
                AlertId.generate(),
                organisationId,
                issuerId,
                content,
                audience,
                level,
                now
        );
        alert.registerEvent(new AlertBroadcast(
                alert.id,
                organisationId,
                issuerId,
                content,
                audience,
                level,
                now
        ));
        return alert;
    }

    /**
     * Reconstruit une alerte existante depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     *
     * @return l'alerte reconstituée
     */
    public static Alert reconstitute(
            AlertId id,
            OrganisationId organisationId,
            UUID issuerId,
            AlertContent content,
            AlertAudience audience,
            AlertLevel level,
            Instant issuedAt
    ) {
        return new Alert(id, organisationId, issuerId, content, audience, level, issuedAt);
    }

    public AlertId id() {
        return id;
    }

    public OrganisationId organisationId() {
        return organisationId;
    }

    public UUID issuerId() {
        return issuerId;
    }

    public AlertContent content() {
        return content;
    }

    public AlertAudience audience() {
        return audience;
    }

    public AlertLevel level() {
        return level;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Alert alert = (Alert) o;
        return Objects.equals(id, alert.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", organisationId=" + organisationId +
                ", issuerId=" + issuerId +
                ", audience=" + audience +
                ", level=" + level +
                '}';
    }
}
