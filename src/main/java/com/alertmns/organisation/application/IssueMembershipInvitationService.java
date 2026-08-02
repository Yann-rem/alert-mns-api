package com.alertmns.organisation.application;

import com.alertmns.identity.domain.port.incoming.RegisterPendingUserUseCase;
import com.alertmns.identity.domain.port.incoming.command.RegisterPendingUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.exception.InvitationAlreadyPendingException;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.port.incoming.IssueMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.incoming.command.IssueMembershipInvitationCommand;
import com.alertmns.organisation.domain.port.outgoing.MembershipInvitationRepository;
import com.alertmns.shared.Email;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

/**
 * Service applicatif orchestrant l'émission d'une invitation à rejoindre une organisation.
 *
 * <p>Parse (VOs) → check (cas B + double invitation) → orchestre (RegisterPendingUser + Invitation) → save →
 * publish.</p>
 */
public final class IssueMembershipInvitationService implements IssueMembershipInvitationUseCase {

    private final MembershipInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final RegisterPendingUserUseCase registerPendingUserUseCase;
    private final EventPublisher publisher;
    private final Clock clock;
    private final Duration ttl;

    public IssueMembershipInvitationService(
            MembershipInvitationRepository invitationRepository,
            UserRepository userRepository,
            RegisterPendingUserUseCase registerPendingUserUseCase,
            EventPublisher publisher,
            Clock clock,
            Duration ttl
    ) {
        this.invitationRepository = Objects.requireNonNull(
                invitationRepository, "invitationRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.registerPendingUserUseCase = Objects.requireNonNull(
                registerPendingUserUseCase, "registerPendingUserUseCase must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.ttl = Objects.requireNonNull(ttl, "ttl must not be null");
    }

    @Override
    public MembershipInvitationId issue(IssueMembershipInvitationCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        Email invitedEmail = Email.of(command.invitedEmail());
        MemberRole role = MemberRole.valueOf(command.role());

        if (userRepository.existsByEmail(invitedEmail)) {
            throw new UnsupportedOperationException(
                    "Inviting an existing user to a new organization is not implemented in the MVP.");
        }

        if (invitationRepository.existsPendingByEmail(invitedEmail)) {
            throw new InvitationAlreadyPendingException(invitedEmail);
        }

        registerPendingUserUseCase.register(new RegisterPendingUserCommand(
                invitedEmail.value(),
                command.firstName(),
                command.lastName()
        ));

        MembershipInvitation invitation = MembershipInvitation.issue(
                organisationId, invitedEmail, role, clock.instant(), ttl);
        invitationRepository.save(invitation);
        publisher.publish(invitation.pullDomainEvents());

        return invitation.id();
    }
}
