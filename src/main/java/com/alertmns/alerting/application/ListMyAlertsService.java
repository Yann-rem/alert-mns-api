package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.port.incoming.ListMyAlertsUseCase;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.organisation.domain.model.Member;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service applicatif listant les alertes destinées à l'utilisateur courant.
 *
 * <p>Resolve (utilisateur courant → Member) → charge les alertes visant toute l'organisation et celles des groupes
 * dont il est membre (via l'ACL {@link GroupMembershipPort}) → fusionne et trie de la plus récente à la plus
 * ancienne. Les destinataires sont calculés par requête (pas de fan-out), à l'image de la liste des conversations.</p>
 */
public final class ListMyAlertsService implements ListMyAlertsUseCase {

    private final CurrentMemberResolver currentMemberResolver;
    private final AlertRepository alertRepository;
    private final GroupMembershipPort groupMembershipPort;

    public ListMyAlertsService(
            CurrentMemberResolver currentMemberResolver,
            AlertRepository alertRepository,
            GroupMembershipPort groupMembershipPort
    ) {
        this.currentMemberResolver = Objects.requireNonNull(
                currentMemberResolver, "currentMemberResolver must not be null");
        this.alertRepository = Objects.requireNonNull(alertRepository, "alertRepository must not be null");
        this.groupMembershipPort = Objects.requireNonNull(groupMembershipPort, "groupMembershipPort must not be null");
    }

    @Override
    public List<Alert> list() {
        Member member = currentMemberResolver.resolveCurrentMember();

        List<Alert> organisationWide = alertRepository.findOrganisationWide(member.organisationId());

        List<UUID> groupIds = groupMembershipPort.groupIdsOf(member.id().value());
        List<Alert> groupAlerts = groupIds.isEmpty()
                ? List.of()
                : alertRepository.findByGroupIdIn(groupIds);

        return Stream.concat(organisationWide.stream(), groupAlerts.stream())
                .sorted(Comparator.comparing(Alert::issuedAt).reversed())
                .toList();
    }
}
