package com.alertmns.messaging.domain.port.incoming;

import com.alertmns.messaging.domain.model.Conversation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Conversation telle qu'affichée dans la liste de l'utilisateur courant.
 *
 * <p>L'agrégat {@link Conversation} ne porte de nom que pour un groupe : un DM n'est qu'une paire de
 * {@code memberId}. Le titre est donc résolu <b>pour le lecteur</b> — c'est le nom de son
 * interlocuteur — ce que seule l'application peut faire, puisqu'elle sait qui demande.</p>
 *
 * @param conversation        l'agrégat
 * @param title               nom du groupe, ou nom de l'interlocuteur pour un DM
 * @param counterpartMemberId l'interlocuteur d'un DM, {@code null} pour un groupe
 * @param lastMessage         dernier message échangé, {@code null} si la conversation est vide
 */
public record ConversationSummary(
        Conversation conversation,
        String title,
        UUID counterpartMemberId,
        LastMessage lastMessage
) {

    public ConversationSummary {
        Objects.requireNonNull(conversation, "conversation must not be null");
        Objects.requireNonNull(title, "title must not be null");
    }

    /**
     * Instant qui ordonne la liste : la dernière activité réelle, à défaut la création.
     *
     * <p>Trier sur {@code createdAt} enfouirait une conversation ancienne mais vivante sous des
     * conversations récentes et muettes.</p>
     */
    public Instant lastActivityAt() {
        return lastMessage == null ? conversation.createdAt() : lastMessage.sentAt();
    }

    /** Aperçu du dernier message, suffisant pour la liste sans charger le fil. */
    public record LastMessage(UUID messageId, UUID authorId, String authorName, String content, Instant sentAt) {}
}
