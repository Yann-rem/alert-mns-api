package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.organisation.domain.port.outgoing.UserDirectoryPort.UserSummary;

import java.util.List;
import java.util.Optional;

/**
 * Port entrant : invitations encore en attente d'acceptation.
 *
 * <p>Complète {@link ListMembersUseCase} : une personne invitée n'a pas encore de {@code Member}
 * (celui-ci n'est créé qu'à l'activation du compte), elle n'apparaît donc pas dans la liste des
 * membres. Le backoffice affiche les deux ensembles.</p>
 *
 * <p>Implémenté par {@code com.alertmns.organisation.application.ListPendingInvitationsService}.</p>
 */
public interface ListPendingInvitationsUseCase {

    /**
     * Une invitation, accompagnée de l'identité de l'utilisateur PENDING créé à son émission.
     * L'identité est optionnelle par prudence : l'invitation reste listée si le {@code User}
     * correspondant est introuvable.
     */
    record PendingInvitation(MembershipInvitation invitation, Optional<UserSummary> user) {}

    List<PendingInvitation> list(String organisationId);
}
