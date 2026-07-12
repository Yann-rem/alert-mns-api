package com.alertmns.messaging.domain.port.outgoing;

import java.time.Instant;
import java.util.UUID;

/**
 * Charge utile poussée aux participants d'une conversation lorsqu'un message y est posté.
 *
 * <p>Contrairement à l'événement {@code AlertBroadcast} (thick), {@code MessagePosted} est thin : cette projection est
 * construite en rechargeant le message et en résolvant le nom de l'auteur au runtime (anonymisation-aware). L'auteur
 * n'est jamais dénormalisé côté domaine — seul {@code authorId} (memberId opaque) est stocké ; {@code authorName} est
 * résolu ici, à la volée. {@code replyToId} est {@code null} pour un message racine.</p>
 */
public record MessageNotification(
        UUID messageId,
        UUID conversationId,
        UUID authorId,
        String authorName,
        String content,
        UUID replyToId,
        Instant sentAt
) {}
