package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.LastAdminCannotBeRemovedException;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.SuspendMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.SuspendMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la suspension des membres.
 *
 * <p>Parse (VOs) → load (agrégat) → check (tenant) → check (invariant « au moins un ADMIN actif », ADR-0013) → act
 * (suspend) → save → publish.</p>
 */
public final class SuspendMemberService implements SuspendMemberUseCase {

    private final MemberRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public SuspendMemberService(MemberRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void suspend(SuspendMemberCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        MemberId memberId = MemberId.from(command.memberId());
        Member member = repository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!member.organisationId().equals(organisationId)) {
            throw new OrganisationMismatchException(member.organisationId(), organisationId);
        }

        guardLastAdmin(member, organisationId);

        member.suspend(clock.instant());
        repository.save(member);
        publisher.publish(member.pullDomainEvents());
    }

    /**
     * Refuse la suspension si elle retire le dernier administrateur actif de l'organisation (ADR-0013).
     */
    private void guardLastAdmin(Member member, OrganisationId organisationId) {
        if (member.role() != MemberRole.ADMIN || member.status() != MemberStatus.ACTIVE) {
            return;
        }
        long activeAdmins = repository.countByOrganisationIdAndRoleAndStatus(
                organisationId, MemberRole.ADMIN, MemberStatus.ACTIVE);
        if (activeAdmins <= 1) {
            throw new LastAdminCannotBeRemovedException();
        }
    }
}
