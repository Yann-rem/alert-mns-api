package com.alertmns.messaging.domain.port.incoming.command;

import java.util.Objects;

/**
 * Commande représentant l'envoi d'un message dans une conversation, éventuellement en réponse à un autre message.
 *
 * <p>{@code replyToMessageId} est optionnel : {@code null} pour un message racine, sinon l'identifiant du message
 * auquel la réponse se rattache. Cette nullité admise explique l'absence de {@code requireNonNull} le concernant.</p>
 */
public record PostMessageCommand(String conversationId, String content, String replyToMessageId) {

    public PostMessageCommand {
        Objects.requireNonNull(conversationId, "conversationId must not be null");
        Objects.requireNonNull(content, "content must not be null");
    }
}
