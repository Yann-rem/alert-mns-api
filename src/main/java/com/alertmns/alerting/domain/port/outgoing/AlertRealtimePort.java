package com.alertmns.alerting.domain.port.outgoing;

import java.util.Collection;
import java.util.UUID;

/**
 * Port sortant de livraison temps réel d'une alerte à un ensemble de destinataires.
 *
 * <p>Abstrait le transport (STOMP/WebSocket) : le domaine et l'application ignorent la technologie sous-jacente. Seuls
 * les destinataires actuellement connectés reçoivent effectivement la notification — pas de stockage, pas de fan-out
 * persistant.</p>
 */
public interface AlertRealtimePort {

    /**
     * Pousse une notification d'alerte à chaque destinataire connecté.
     *
     * @param recipientUserIds les {@code userId} des destinataires
     * @param notification     la charge utile à livrer
     */
    void push(Collection<UUID> recipientUserIds, AlertNotification notification);
}
