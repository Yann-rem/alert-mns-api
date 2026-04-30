package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.ActivateMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ActivateMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de l'activation des membres.
 *
 * <p>Parse (VOs) → load (agrégat) → check (tenant) → act (activate) → save → publish.</p>
 */
public final class ActivateMemberService implements ActivateMemberUseCase {

    private final MemberRepository repository;
    private final EventPublisher publisher;

    public ActivateMemberService(MemberRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public void activate(ActivateMemberCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        MemberId memberId = MemberId.from(command.memberId());
        Member member = repository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!member.organisationId().equals(organisationId)) {
            throw new OrganisationMismatchException(member.organisationId(), organisationId);
        }

        member.activate();
        repository.save(member);
        publisher.publish(member.pullDomainEvents());
    }
}
