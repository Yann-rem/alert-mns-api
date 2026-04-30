package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.ReactivateMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ReactivateMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la réactivation des membres.
 *
 * <p>Parse (VOs) → load (agrégat) → check (tenant) → act (reactivate) → save → publish.</p>
 */
public final class ReactivateMemberService implements ReactivateMemberUseCase {

    private final MemberRepository repository;
    private final EventPublisher publisher;

    public ReactivateMemberService(MemberRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public void reactivate(ReactivateMemberCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        MemberId memberId = MemberId.from(command.memberId());
        Member member = repository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!member.organisationId().equals(organisationId)) {
            throw new OrganisationMismatchException(member.organisationId(), organisationId);
        }

        member.reactivate();
        repository.save(member);
        publisher.publish(member.pullDomainEvents());
    }
}
