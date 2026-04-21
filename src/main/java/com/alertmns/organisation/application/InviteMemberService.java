package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.MemberAlreadyExistsException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.port.incoming.InviteMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.util.Objects;
import java.util.UUID;

/**
 * Service applicatif représentant l'orchestration de l'invitation des membres.
 *
 * <p>Parse (VOs + role) → check (unicité du membre dans l'organisation) → act (Member.invite) → save → publish.</p>
 */
public final class InviteMemberService implements InviteMemberUseCase {

    private final MemberRepository repository;
    private final EventPublisher publisher;

    public InviteMemberService(MemberRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public MemberId invite(InviteMemberCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        UUID userId = UUID.fromString(command.userId());
        MemberRole role = MemberRole.valueOf(command.role());

        if (repository.existsByOrganisationIdAndUserId(organisationId, userId)) {
            throw new MemberAlreadyExistsException(organisationId, userId);
        }

        Member member = Member.invite(organisationId, userId, role);
        repository.save(member);
        publisher.publish(member.pullDomainEvents());
        return member.id();
    }
}
