package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertAudience;
import com.alertmns.alerting.domain.model.AlertAudienceKind;
import com.alertmns.alerting.domain.model.AlertContent;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.model.AlertLevel;
import com.alertmns.alerting.domain.port.incoming.BroadcastAlertUseCase;
import com.alertmns.alerting.domain.port.incoming.command.BroadcastAlertCommand;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.shared.EventPublisher;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * Service applicatif orchestrant la diffusion d'une alerte.
 *
 * <p>Resolve (utilisateur courant → Member émetteur) → parse (contenu, niveau, audience) → act
 * ({@link Alert#broadcast}) → save → publish. L'autorisation par rôle (ADMIN/MANAGER) est portée par l'adapter
 * entrant, pas par ce service.</p>
 */
public final class BroadcastAlertService implements BroadcastAlertUseCase {

    private final CurrentMemberResolver currentMemberResolver;
    private final AlertRepository alertRepository;
    private final EventPublisher publisher;
    private final Clock clock;

    public BroadcastAlertService(
            CurrentMemberResolver currentMemberResolver,
            AlertRepository alertRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        this.currentMemberResolver = Objects.requireNonNull(
                currentMemberResolver, "currentMemberResolver must not be null");
        this.alertRepository = Objects.requireNonNull(alertRepository, "alertRepository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public AlertId broadcast(BroadcastAlertCommand command) {
        Member issuer = currentMemberResolver.resolveCurrentMember();

        AlertContent content = AlertContent.of(command.content());
        AlertLevel level = parseLevel(command.level());
        AlertAudience audience = parseAudience(command.audienceKind(), command.groupId());

        Alert alert = Alert.broadcast(
                issuer.organisationId(),
                issuer.id().value(),
                content,
                audience,
                level,
                clock.instant()
        );
        alertRepository.save(alert);
        publisher.publish(alert.pullDomainEvents());
        return alert.id();
    }

    private AlertLevel parseLevel(String level) {
        try {
            return AlertLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("level must be one of INFO, IMPORTANT, URGENT");
        }
    }

    private AlertAudience parseAudience(String audienceKind, String groupId) {
        return switch (parseAudienceKind(audienceKind)) {
            case ORGANISATION -> AlertAudience.organisation();
            case GROUP -> AlertAudience.group(parseGroupId(groupId));
        };
    }

    private AlertAudienceKind parseAudienceKind(String audienceKind) {
        try {
            return AlertAudienceKind.valueOf(audienceKind);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("audienceKind must be one of ORGANISATION, GROUP");
        }
    }

    private UUID parseGroupId(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("groupId is required for a GROUP audience");
        }
        return UUID.fromString(groupId);
    }
}
