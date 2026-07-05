package com.alertmns.alerting.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object décrivant la cible d'une alerte.
 *
 * <p>Une alerte vise soit toute l'organisation ({@link AlertAudienceKind#ORGANISATION}), soit un groupe précis
 * ({@link AlertAudienceKind#GROUP}). L'invariant garantit que {@code groupId} est présent si et seulement si le genre
 * est {@link AlertAudienceKind#GROUP}.</p>
 */
public record AlertAudience(AlertAudienceKind kind, UUID groupId) {

    public AlertAudience {
        Objects.requireNonNull(kind, "audience kind must not be null");
        switch (kind) {
            case ORGANISATION -> {
                if (groupId != null) {
                    throw new IllegalArgumentException("groupId must be null for an ORGANISATION audience");
                }
            }
            case GROUP -> Objects.requireNonNull(groupId, "groupId must not be null for a GROUP audience");
        }
    }

    /**
     * Audience visant toute l'organisation.
     *
     * @return une audience de genre ORGANISATION
     */
    public static AlertAudience organisation() {
        return new AlertAudience(AlertAudienceKind.ORGANISATION, null);
    }

    /**
     * Audience visant un groupe précis.
     *
     * @param groupId identifiant du groupe ciblé
     * @return une audience de genre GROUP
     */
    public static AlertAudience group(UUID groupId) {
        return new AlertAudience(AlertAudienceKind.GROUP, groupId);
    }
}
