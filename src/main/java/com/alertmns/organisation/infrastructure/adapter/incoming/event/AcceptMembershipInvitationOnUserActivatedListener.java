package com.alertmns.organisation.infrastructure.adapter.incoming.event;

import com.alertmns.identity.domain.event.UserActivated;
import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.port.incoming.AcceptMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.incoming.command.AcceptMembershipInvitationCommand;
import com.alertmns.organisation.domain.port.outgoing.MembershipInvitationRepository;
import org.springframework.context.event.EventListener;

import java.util.Objects;

/**
 * Listener qui accepte l'invitation en attente lorsqu'un User est activé via le redeem du magic-link.
 */
public final class AcceptMembershipInvitationOnUserActivatedListener {

    private final MembershipInvitationRepository invitationRepository;
    private final AcceptMembershipInvitationUseCase acceptMembershipInvitationUseCase;

    public AcceptMembershipInvitationOnUserActivatedListener(
            MembershipInvitationRepository invitationRepository,
            AcceptMembershipInvitationUseCase acceptMembershipInvitationUseCase
    ) {
        this.invitationRepository = Objects.requireNonNull(
                invitationRepository, "invitationRepository must not be null");
        this.acceptMembershipInvitationUseCase = Objects.requireNonNull(
                acceptMembershipInvitationUseCase, "acceptMembershipInvitationUseCase must not be null");
    }

    @EventListener
    public void onUserActivatedEvent(UserActivated event) {
        MembershipInvitation invitation = invitationRepository
                .findPendingByEmail(event.email())
                .orElseThrow(() -> new IllegalStateException(
                        "No PENDING invitation found for activated user email: " + event.email().value()));

        acceptMembershipInvitationUseCase.accept(
                new AcceptMembershipInvitationCommand(
                        invitation.id().value().toString(),
                        event.userId().value().toString()
                )
        );
    }
}
