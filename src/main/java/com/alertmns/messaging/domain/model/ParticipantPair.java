package com.alertmns.messaging.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object représentant la paire ordonnée des participants d'une conversation directe.
 *
 * <p>Les deux identifiants sont distincts et rangés dans un ordre canonique (plus petit en premier), garantissant que
 * la paire {@code {A,B}} est identique à {@code {B,A}}.</p>
 */
public record ParticipantPair(UUID low, UUID high) {

    public ParticipantPair {
        Objects.requireNonNull(low, "low must not be null");
        Objects.requireNonNull(high, "high must not be null");
    }

    /**
     * Crée la paire canonique à partir de deux membres distincts.
     *
     * @throws IllegalArgumentException si les deux membres sont identiques
     */
    public static ParticipantPair of(UUID firstMemberId, UUID secondMemberId) {
        Objects.requireNonNull(firstMemberId, "firstMemberId must not be null");
        Objects.requireNonNull(secondMemberId, "secondMemberId must not be null");
        if (firstMemberId.equals(secondMemberId)) {
            throw new IllegalArgumentException("A direct conversation requires two distinct members");
        }
        UUID low = firstMemberId.compareTo(secondMemberId) < 0 ? firstMemberId : secondMemberId;
        UUID high = low == firstMemberId ? secondMemberId : firstMemberId;
        return new ParticipantPair(low, high);
    }

    /**
     * Indique si le membre donné fait partie de la paire.
     */
    public boolean contains(UUID memberId) {
        return low.equals(memberId) || high.equals(memberId);
    }
}
