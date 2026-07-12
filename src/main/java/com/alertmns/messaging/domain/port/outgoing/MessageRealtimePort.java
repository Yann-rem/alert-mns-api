package com.alertmns.messaging.domain.port.outgoing;

import java.util.Collection;
import java.util.UUID;

/**
 * Port sortant de livraison temps réel d'un message aux participants d'une conversation.
 *
 * <p>Abstrait le transport (STOMP/WebSocket). Seuls les destinataires actuellement connectés reçoivent la
 * notification.</p>
 */
public interface MessageRealtimePort {

    /**
     * Pousse une notification de message à chaque destinataire connecté.
     *
     * @param recipientUserIds les {@code userId} des destinataires
     * @param notification     la charge utile à livrer
     */
    void push(Collection<UUID> recipientUserIds, MessageNotification notification);
}
