package com.alertmns.iam.domain.model;

import com.alertmns.iam.domain.exception.ActivationTokenExpiredException;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.UserId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Agrégat racine représentant un token d'activation à usage unique.
 *
 * <p>Émis par le BC IAM lors de l'inscription, transmis à l'utilisateur via un lien magique, puis consommé pour la
 * transition {@code PENDING → ACTIVE} de l'utilisateur cible.</p>
 *
 * <p>Le raw token n'est jamais persisté : seul son {@link TokenHash} est stocké. Le token est supprimé après
 * consommation (pas de soft-delete).</p>
 */
public final class ActivationToken extends AggregateRoot {

    private final ActivationTokenId id;
    private final UserId userId;
    private final TokenHash tokenHash;
    private final Instant createdAt;
    private final Instant expiresAt;

    private ActivationToken(
            ActivationTokenId id,
            UserId userId,
            TokenHash tokenHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    /**
     * Émet un nouveau token d'activation pour un utilisateur.
     *
     * <p>L'identifiant et la date de création sont générés automatiquement. La date d'expiration est calculée par
     * {@code createdAt + ttl}.</p>
     *
     * @param userId   l'identifiant de l'utilisateur cible
     * @param rawToken le token brut (sera haché en SHA-256 et seul le hash sera persisté)
     * @param ttl      la durée de validité du token
     * @return un {@link IssuedToken} portant l'agrégat et le raw token (à transmettre une seule fois)
     */
    public static IssuedToken issue(UserId userId, RawToken rawToken, Duration ttl) {
        Instant now = Instant.now();
        ActivationToken activationToken = new ActivationToken(
                ActivationTokenId.generate(),
                userId,
                TokenHash.of(rawToken),
                now,
                now.plus(ttl)
        );

        return new IssuedToken(activationToken, rawToken);
    }

    /**
     * Reconstruit un token d'activation depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     */
    public static ActivationToken reconstitute(
            ActivationTokenId id,
            UserId userId,
            TokenHash tokenHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new ActivationToken(id, userId, tokenHash, createdAt, expiresAt);
    }

    /**
     * Vérifie que le token est encore utilisable au moment courant.
     *
     * @throws ActivationTokenExpiredException si le token a dépassé sa date d'expiration
     */
    public void verifyUsable() {
        if (isExpired()) {
            throw new ActivationTokenExpiredException(id);
        }
    }

    /**
     * Indique si le token a dépassé sa date d'expiration.
     *
     * @return {@code true} si {@code now > expiresAt}
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public ActivationTokenId id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public TokenHash tokenHash() {
        return tokenHash;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivationToken that = (ActivationToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ActivationToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }

    /**
     * Résultat de {@link ActivationToken#issue}. Encapsule l'agrégat persistable et le raw token à transmettre une
     * seule fois à l'utilisateur (par e-mail).
     */
    public record IssuedToken(ActivationToken activationToken, RawToken rawToken) {}
}
