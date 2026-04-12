package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant la suspension d'un membre.
 *
 * <p>Le membre passe du statut ACTIVE à SUSPENDED.</p>
 */
public final class MemberSuspended implements DomainEvent {

    private final MemberId memberId;
    private final Instant occurredOn;

    public MemberSuspended(MemberId memberId) {
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
        MemberSuspended that = (MemberSuspended) o;
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, occurredOn);
    }

    @Override
    public String toString() {
        return "MemberSuspended{" +
                "memberId=" + memberId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
