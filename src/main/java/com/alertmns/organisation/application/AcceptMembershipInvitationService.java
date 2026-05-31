package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.MembershipInvitationNotFoundException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.port.incoming.AcceptMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.incoming.command.AcceptMembershipInvitationCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.domain.port.outgoing.MembershipInvitationRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.MembershipInvitationId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service applicatif orchestrant l'acceptation d'une invitation.
 *
 * <p>Charge l'invitation → délègue la transition à l'agrégat ({@code invitation.accept}) → sauvegarde → crée le
 * {@code Member} directement en {@link MemberStatus#ACTIVE} via {@link Member#createActive} → sauvegarde → publie
 * l'ensemble des événements en une seule fois ({@code MembershipInvitationAccepted} + {@code MemberJoined}).</p>
 */
public final class AcceptMembershipInvitationService implements AcceptMembershipInvitationUseCase {

    private final MembershipInvitationRepository invitationRepository;
    private final MemberRepository memberRepository;
    private final EventPublisher publisher;

    public AcceptMembershipInvitationService(
            MembershipInvitationRepository invitationRepository,
            MemberRepository memberRepository,
            EventPublisher publisher
    ) {
        this.invitationRepository = Objects.requireNonNull(
                invitationRepository, "invitationRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public void accept(AcceptMembershipInvitationCommand command) {
        MembershipInvitationId invitationId = MembershipInvitationId.from(command.invitationId());
        UUID userId = UUID.fromString(command.userId());

        MembershipInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new MembershipInvitationNotFoundException(invitationId));

        invitation.accept(Instant.now(), userId);
        invitationRepository.save(invitation);

        Member member = Member.createActive(invitation.organisationId(), userId, invitation.role());
        memberRepository.save(member);

        List<DomainEvent> events = new ArrayList<>();
        events.addAll(invitation.pullDomainEvents());
        events.addAll(member.pullDomainEvents());
        publisher.publish(events);
    }
}
