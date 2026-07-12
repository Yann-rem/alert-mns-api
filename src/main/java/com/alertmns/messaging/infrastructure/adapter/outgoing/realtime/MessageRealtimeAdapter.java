package com.alertmns.messaging.infrastructure.adapter.outgoing.realtime;

import com.alertmns.messaging.domain.port.outgoing.MessageNotification;
import com.alertmns.messaging.domain.port.outgoing.MessageRealtimePort;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collection;
import java.util.UUID;

/**
 * Adapter livrant les notifications de message sur des <em>user-destinations</em> STOMP.
 *
 * <p>Pour chaque destinataire, envoie la charge utile sur {@code /user/{userId}/queue/messages} via
 * {@link SimpMessagingTemplate#convertAndSendToUser}. Le nom d'utilisateur STOMP est le {@code userId} (posé au
 * handshake). La livraison effective est asynchrone (canal sortant STOMP).</p>
 */
public final class MessageRealtimeAdapter implements MessageRealtimePort {

    static final String DESTINATION = "/queue/messages";

    private final SimpMessagingTemplate messagingTemplate;

    public MessageRealtimeAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void push(Collection<UUID> recipientUserIds, MessageNotification notification) {
        for (UUID userId : recipientUserIds) {
            messagingTemplate.convertAndSendToUser(userId.toString(), DESTINATION, notification);
        }
    }
}
