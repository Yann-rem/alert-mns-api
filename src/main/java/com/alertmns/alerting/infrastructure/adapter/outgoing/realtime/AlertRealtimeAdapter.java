package com.alertmns.alerting.infrastructure.adapter.outgoing.realtime;

import com.alertmns.alerting.domain.port.outgoing.AlertNotification;
import com.alertmns.alerting.domain.port.outgoing.AlertRealtimePort;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collection;
import java.util.UUID;

/**
 * Adapter livrant les notifications d'alerte sur des <em>user-destinations</em> STOMP.
 *
 * <p>Pour chaque destinataire, envoie la charge utile sur {@code /user/{userId}/queue/alerts} via
 * {@link SimpMessagingTemplate#convertAndSendToUser}. Le nom d'utilisateur STOMP est le {@code userId} (posé au
 * handshake), ce qui aligne le routage sur la résolution des destinataires. La sérialisation JSON est prise en charge
 * par le convertisseur de messages configuré. La livraison effective est asynchrone (canal sortant STOMP).</p>
 */
public final class AlertRealtimeAdapter implements AlertRealtimePort {

    static final String DESTINATION = "/queue/alerts";

    private final SimpMessagingTemplate messagingTemplate;

    public AlertRealtimeAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void push(Collection<UUID> recipientUserIds, AlertNotification notification) {
        for (UUID userId : recipientUserIds) {
            messagingTemplate.convertAndSendToUser(userId.toString(), DESTINATION, notification);
        }
    }
}
