package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.LastAdminCannotBeRemovedException;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.ChangeMemberRoleUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ChangeMemberRoleCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif orchestrant le changement de rôle d'un membre.
 *
 * <p>Parse (VOs + rôle) → load (agrégat) → check (tenant) → check (invariant « au moins un ADMIN actif », ADR-0013)
 * → act (changeRole) → save → publish.</p>
 */
public final class ChangeMemberRoleService implements ChangeMemberRoleUseCase {

    private final MemberRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public ChangeMemberRoleService(MemberRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void changeRole(ChangeMemberRoleCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        MemberId memberId = MemberId.from(command.memberId());
        MemberRole newRole = parseRole(command.role());

        Member member = repository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!member.organisationId().equals(organisationId)) {
            throw new OrganisationMismatchException(member.organisationId(), organisationId);
        }

        guardLastAdmin(member, newRole, organisationId);

        member.changeRole(newRole, clock.instant());
        repository.save(member);
        publisher.publish(member.pullDomainEvents());
    }

    /**
     * Refuse l'opération si elle rétrograde le dernier administrateur actif de l'organisation (ADR-0013).
     */
    private void guardLastAdmin(Member member, MemberRole newRole, OrganisationId organisationId) {
        boolean demotingActiveAdmin = member.role() == MemberRole.ADMIN
                && member.status() == MemberStatus.ACTIVE
                && newRole != MemberRole.ADMIN;
        if (!demotingActiveAdmin) {
            return;
        }
        long activeAdmins = repository.countByOrganisationIdAndRoleAndStatus(
                organisationId, MemberRole.ADMIN, MemberStatus.ACTIVE);
        if (activeAdmins <= 1) {
            throw new LastAdminCannotBeRemovedException();
        }
    }

    private MemberRole parseRole(String role) {
        try {
            return MemberRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("role must be one of ADMIN, MANAGER, MEMBER");
        }
    }
}
