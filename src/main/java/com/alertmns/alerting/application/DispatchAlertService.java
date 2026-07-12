package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.event.AlertBroadcast;
import com.alertmns.alerting.domain.port.outgoing.AlertNotification;
import com.alertmns.alerting.domain.port.outgoing.AlertRealtimePort;
import com.alertmns.alerting.domain.port.outgoing.AlertRecipientPort;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service applicatif livrant une alerte diffusée à ses destinataires en temps réel.
 *
 * <p>Réaction à l'événement {@link AlertBroadcast} : resolve (audience → {@code userId} des destinataires) → build
 * (projection {@link AlertNotification}) → push. Aucune donnée n'est rechargée : l'événement thick porte tout. Si
 * l'audience ne résout aucun destinataire, aucun push n'est émis.</p>
 */
public final class DispatchAlertService {

    private final AlertRecipientPort recipientPort;
    private final AlertRealtimePort realtimePort;

    public DispatchAlertService(AlertRecipientPort recipientPort, AlertRealtimePort realtimePort) {
        this.recipientPort = Objects.requireNonNull(recipientPort, "recipientPort must not be null");
        this.realtimePort = Objects.requireNonNull(realtimePort, "realtimePort must not be null");
    }

    public void dispatch(AlertBroadcast event) {
        List<UUID> recipients = resolveRecipients(event);
        if (recipients.isEmpty()) {
            return;
        }
        realtimePort.push(recipients, toNotification(event));
    }

    private List<UUID> resolveRecipients(AlertBroadcast event) {
        return switch (event.audience().kind()) {
            case ORGANISATION -> recipientPort.organisationRecipients(event.organisationId());
            case GROUP -> recipientPort.groupRecipients(event.audience().groupId());
        };
    }

    private AlertNotification toNotification(AlertBroadcast event) {
        return new AlertNotification(
                event.alertId().value(),
                event.organisationId().value(),
                event.issuerId(),
                event.content().value(),
                event.level().name(),
                event.audience().kind().name(),
                event.audience().groupId(),
                event.occurredOn()
        );
    }
}
