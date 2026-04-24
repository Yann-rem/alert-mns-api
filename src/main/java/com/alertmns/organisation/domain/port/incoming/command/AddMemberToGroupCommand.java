package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande d'ajout d'un membre à un groupe.
 *
 * <p>Valeurs brutes — le service applicatif est responsable de la création des VO
 * ({@link com.alertmns.shared.OrganisationId},
 * {@link com.alertmns.organisation.domain.model.GroupId},
 * {@link com.alertmns.organisation.domain.model.MemberId}).</p>
 */
public record AddMemberToGroupCommand(String organisationId, String groupId, String memberId) {

    public AddMemberToGroupCommand {
        Objects.requireNonNull(organisationId, "organisationId must not be null");
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
    }
}
