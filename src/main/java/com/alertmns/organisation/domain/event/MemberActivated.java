package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant l'activation d'un membre.
 *
 * <p>Le membre passe du statut PENDING à ACTIVE.</p>
 */
public final class MemberActivated implements DomainEvent {

    private final MemberId memberId;
    private final Instant occurredOn;

    public MemberActivated(MemberId memberId) {
        this.memberId = memberId;
        occurredOn = Instant.now();
    }

    public MemberId memberId() {
        return memberId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MemberActivated that = (MemberActivated) o;
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, occurredOn);
    }

    @Override
    public String toString() {
        return "MemberActivated{" +
                "memberId=" + memberId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
