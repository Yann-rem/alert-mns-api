package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant la réactivation d'un membre suspendu.
 *
 * <p>Le membre passe du statut SUSPENDED à ACTIVE.</p>
 */
public final class MemberReactivated implements DomainEvent {

    private final MemberId memberId;
    private final Instant occurredOn;

    public MemberReactivated(MemberId memberId) {
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
        MemberReactivated that = (MemberReactivated) o;
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, occurredOn);
    }

    @Override
    public String toString() {
        return "MemberReactivated{" +
                "memberId=" + memberId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
