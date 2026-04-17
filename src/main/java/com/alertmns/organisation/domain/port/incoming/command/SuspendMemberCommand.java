package com.alertmns.organisation.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant la demande de suspension d'un membre.
 *
 * <p>Valeur brute — le service applicatif est responsable de la création du VO
 * {@link com.alertmns.organisation.domain.model.MemberId}.</p>
 */
public record SuspendMemberCommand(String memberId) {
    public SuspendMemberCommand {
        Objects.requireNonNull(memberId, "memberId must not be null");
    }
}
