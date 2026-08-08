package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.port.incoming.AlertView;
import com.alertmns.alerting.domain.port.incoming.ListMyAlertsUseCase;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.domain.port.outgoing.GroupDirectoryPort;
import com.alertmns.alerting.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.alerting.domain.port.outgoing.IssuerDirectoryPort;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.organisation.domain.model.Member;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service applicatif listant les alertes destinées à l'utilisateur courant.
 *
 * <p>Resolve (utilisateur courant → Member) → charge les alertes visant toute l'organisation et celles des groupes
 * dont il est membre (via l'ACL {@link GroupMembershipPort}) → fusionne, trie de la plus récente à la plus ancienne,
 * puis nomme émetteur et groupe cible. Les destinataires sont calculés par requête (pas de fan-out), à l'image de la
 * liste des conversations.</p>
 */
public final class ListMyAlertsService implements ListMyAlertsUseCase {

    private final CurrentMemberResolver currentMemberResolver;
    private final AlertRepository alertRepository;
    private final GroupMembershipPort groupMembershipPort;
    private final IssuerDirectoryPort issuerDirectory;
    private final GroupDirectoryPort groupDirectory;

    public ListMyAlertsService(
            CurrentMemberResolver currentMemberResolver,
            AlertRepository alertRepository,
            GroupMembershipPort groupMembershipPort,
            IssuerDirectoryPort issuerDirectory,
            GroupDirectoryPort groupDirectory
    ) {
        this.currentMemberResolver = Objects.requireNonNull(
                currentMemberResolver, "currentMemberResolver must not be null");
        this.alertRepository = Objects.requireNonNull(alertRepository, "alertRepository must not be null");
        this.groupMembershipPort = Objects.requireNonNull(groupMembershipPort, "groupMembershipPort must not be null");
        this.issuerDirectory = Objects.requireNonNull(issuerDirectory, "issuerDirectory must not be null");
        this.groupDirectory = Objects.requireNonNull(groupDirectory, "groupDirectory must not be null");
    }

    @Override
    public List<AlertView> list() {
        Member member = currentMemberResolver.resolveCurrentMember();

        List<Alert> organisationWide = alertRepository.findOrganisationWide(member.organisationId());

        List<UUID> groupIds = groupMembershipPort.groupIdsOf(member.id().value());
        List<Alert> groupAlerts = groupIds.isEmpty()
                ? List.of()
                : alertRepository.findByGroupIdIn(groupIds);

        // Mémoïsation locale à l'appel : une même personne diffuse souvent plusieurs alertes, et un même groupe en
        // reçoit plusieurs. Sans elle, la liste ferait une résolution par alerte.
        Map<UUID, String> issuerNames = new HashMap<>();
        Map<UUID, String> groupNames = new HashMap<>();

        return Stream.concat(organisationWide.stream(), groupAlerts.stream())
                .sorted(Comparator.comparing(Alert::issuedAt).reversed())
                .map(alert -> toView(alert, issuerNames, groupNames))
                .toList();
    }

    private AlertView toView(Alert alert, Map<UUID, String> issuerNames, Map<UUID, String> groupNames) {
        UUID groupId = alert.audience().groupId();
        return new AlertView(
                alert,
                issuerNames.computeIfAbsent(alert.issuerId(), issuerDirectory::displayNameOf),
                // Reste null pour une audience organisation : il n'y a pas de groupe à nommer.
                groupId == null ? null : groupNames.computeIfAbsent(groupId, groupDirectory::nameOf)
        );
    }
}
