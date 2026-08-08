package com.alertmns.alerting.domain.port.outgoing;

import java.time.Instant;
import java.util.UUID;

/**
 * Charge utile poussée aux destinataires d'une alerte sur le canal temps réel.
 *
 * <p>Projection plate et self-contained de l'événement {@code AlertBroadcast} (event thick) : elle porte tout ce dont
 * le client a besoin pour afficher la notification, sans recharger l'agrégat. Types primitifs / chaînes pour une
 * sérialisation JSON directe sur STOMP.</p>
 *
 * <p>Les noms accompagnent les identifiants pour la même raison qu'en lecture REST : le destinataire ne peut résoudre
 * ni le membre émetteur ni le groupe ciblé, ces annuaires étant réservés aux rôles d'administration. {@code groupId}
 * et {@code groupName} sont {@code null} pour une audience organisation.</p>
 */
public record AlertNotification(
        UUID alertId,
        UUID organisationId,
        UUID issuerId,
        String issuerName,
        String content,
        String level,
        String audienceKind,
        UUID groupId,
        String groupName,
        Instant issuedAt
) {}
