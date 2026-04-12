package com.alertmns.organisation.domain.event;

import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Événement de domaine représentant l'invitation d'un membre dans une organisation.
 *
 * <p>Le membre est créé avec le statut PENDING, en attente d'activation.</p>
 */
public final class MemberInvited implements DomainEvent {

    private final MemberId memberId;
    private final Instant occurredOn;

    public MemberInvited(MemberId memberId) {
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
        MemberInvited that = (MemberInvited) o;
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, occurredOn);
    }

    @Override
    public String toString() {
        return "MemberInvited{" +
                "memberId=" + memberId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
