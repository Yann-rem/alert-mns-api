package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.ListMembersUseCase;
import com.alertmns.organisation.domain.port.incoming.MemberDirectoryEntry;
import com.alertmns.organisation.domain.port.incoming.command.ListMembersQuery;
import com.alertmns.organisation.domain.port.outgoing.MemberFilters;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.domain.port.outgoing.UserDirectoryPort;
import com.alertmns.organisation.domain.port.outgoing.UserDirectoryPort.UserSummary;
import com.alertmns.shared.OrganisationId;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Liste les membres d'une organisation pour le backoffice.
 *
 * <p>Compose deux bounded contexts : les membres (rôle, statut) viennent d'<i>Organisation</i>,
 * leur identité (nom, e-mail) d'<i>Identity</i> via {@link UserDirectoryPort}.</p>
 *
 * <p><b>Ordre des opérations</b> : la recherche textuelle porte sur des champs qui n'existent que
 * dans Identity. Elle est donc résolue <b>avant</b> la requête paginée, sous forme d'une liste
 * d'identifiants qui restreint le filtre SQL — sinon la pagination porterait sur un ensemble
 * différent de celui réellement affiché, faussant totaux et numéros de page.</p>
 */
public class ListMembersService implements ListMembersUseCase {

    private final MemberRepository memberRepository;
    private final UserDirectoryPort userDirectory;

    public ListMembersService(MemberRepository memberRepository, UserDirectoryPort userDirectory) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.userDirectory = Objects.requireNonNull(userDirectory, "userDirectory must not be null");
    }

    @Override
    public MembersPage list(ListMembersQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        OrganisationId organisationId = OrganisationId.from(query.organisationId());
        MemberStatus status = parse(query.status(), MemberStatus.class);
        MemberRole role = parse(query.role(), MemberRole.class);

        List<UUID> matchingUserIds = resolveSearch(query.search());
        // Recherche sans résultat : inutile d'interroger la base, la page est vide par construction.
        if (matchingUserIds != null && matchingUserIds.isEmpty()) {
            return new MembersPage(List.of(), 0L);
        }

        MemberFilters filters = new MemberFilters(status, role, matchingUserIds);
        List<Member> members = memberRepository.findByOrganisationId(
                organisationId, filters, query.page(), query.size());
        long total = memberRepository.countByOrganisationId(organisationId, filters);

        return new MembersPage(enrich(members), total);
    }

    /** @return {@code null} si aucune recherche, sinon les identifiants correspondants (éventuellement vides) */
    private List<UUID> resolveSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return userDirectory.searchIds(search.trim());
    }

    /** Associe à chaque membre son identité, en un seul appel au BC Identity (pas de N+1). */
    private List<MemberDirectoryEntry> enrich(List<Member> members) {
        if (members.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = members.stream().map(Member::userId).toList();
        Map<UUID, UserSummary> byUserId = userDirectory.findByIds(userIds).stream()
                .collect(Collectors.toMap(UserSummary::userId, Function.identity(), (first, ignored) -> first));

        return members.stream()
                .map(member -> new MemberDirectoryEntry(
                        member, Optional.ofNullable(byUserId.get(member.userId()))))
                .toList();
    }

    /** Convertit une valeur d'énumération reçue en texte, en tolérant l'absence de filtre. */
    private static <E extends Enum<E>> E parse(String value, Class<E> type) {
        return (value == null || value.isBlank()) ? null : Enum.valueOf(type, value.trim().toUpperCase());
    }
}
