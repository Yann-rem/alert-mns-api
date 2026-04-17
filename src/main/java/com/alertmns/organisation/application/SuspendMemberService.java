package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.SuspendMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.SuspendMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la suspension des membres.
 *
 * <p>Charge l'agrégat → suspend → persiste → publie les événements.</p>
 */
public final class SuspendMemberService implements SuspendMemberUseCase {

    private final MemberRepository repository;
    private final EventPublisher publisher;

    public SuspendMemberService(MemberRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(
                repository,
                "repository must not be null"
        );

        this.publisher = Objects.requireNonNull(
                publisher,
                "publisher must not be null"
        );
    }

    @Override
    public void suspend(SuspendMemberCommand command) {
        MemberId id = MemberId.from(command.memberId());
        Member member = repository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        member.suspend();
        repository.save(member);
        publisher.publish(member.pullDomainEvents());
    }
}
