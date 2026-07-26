package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.port.incoming.command.ListMembersQuery;

import java.util.List;

/**
 * Port entrant : liste paginée et filtrable des membres d'une organisation, enrichie de
 * l'identité des utilisateurs (cf. ADR-0014, lecture par agrégats).
 *
 * <p>Ne renvoie que de véritables membres. Les personnes invitées mais pas encore activées
 * n'ont pas de {@code Member} : elles relèvent des invitations en attente, exposées séparément.</p>
 *
 * <p>Implémenté par {@code com.alertmns.organisation.application.ListMembersService}.</p>
 */
public interface ListMembersUseCase {

    /** Page de résultats, accompagnée du total correspondant aux filtres. */
    record MembersPage(List<MemberDirectoryEntry> entries, long total) {}

    MembersPage list(ListMembersQuery query);
}
