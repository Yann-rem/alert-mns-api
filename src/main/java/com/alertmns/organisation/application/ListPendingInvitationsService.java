package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.model.MembershipInvitationStatus;
import com.alertmns.organisation.domain.port.incoming.ListPendingInvitationsUseCase;
import com.alertmns.organisation.domain.port.outgoing.MembershipInvitationRepository;
import com.alertmns.organisation.domain.port.outgoing.UserDirectoryPort;
import com.alertmns.organisation.domain.port.outgoing.UserDirectoryPort.UserSummary;
import com.alertmns.shared.OrganisationId;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Liste les invitations en attente d'une organisation, enrichies du prénom et du nom saisis lors
 * de l'invitation (portés par le {@code User} PENDING créé à ce moment-là, côté BC Identity).
 */
public class ListPendingInvitationsService implements ListPendingInvitationsUseCase {

    private final MembershipInvitationRepository invitationRepository;
    private final UserDirectoryPort userDirectory;

    public ListPendingInvitationsService(
            MembershipInvitationRepository invitationRepository, UserDirectoryPort userDirectory) {
        this.invitationRepository = Objects.requireNonNull(
                invitationRepository, "invitationRepository must not be null");
        this.userDirectory = Objects.requireNonNull(userDirectory, "userDirectory must not be null");
    }

    @Override
    public List<PendingInvitation> list(String organisationId) {
        Objects.requireNonNull(organisationId, "organisationId must not be null");

        List<MembershipInvitation> invitations = invitationRepository.findByOrganisationIdAndStatus(
                OrganisationId.from(organisationId), MembershipInvitationStatus.PENDING);
        if (invitations.isEmpty()) {
            return List.of();
        }

        List<String> emails = invitations.stream().map(i -> i.invitedEmail().value()).toList();
        // Comparaison insensible à la casse : l'e-mail stocké sur l'invitation et celui du User
        // proviennent de la même saisie, mais rien ne garantit une casse identique.
        Map<String, UserSummary> byEmail = userDirectory.findByEmails(emails).stream()
                .collect(Collectors.toMap(
                        user -> user.email().toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (first, ignored) -> first));

        return invitations.stream()
                .map(invitation -> new PendingInvitation(
                        invitation,
                        Optional.ofNullable(
                                byEmail.get(invitation.invitedEmail().value().toLowerCase(Locale.ROOT)))))
                .toList();
    }
}
