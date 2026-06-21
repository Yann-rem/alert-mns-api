package com.alertmns.messaging.domain.model;

import com.alertmns.messaging.domain.event.MessagePosted;
import com.alertmns.shared.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrégat racine représentant un message dans le BC Messaging.
 *
 * <p>Un message appartient à une conversation et est immuable une fois envoyé. Il peut référencer un autre message
 * de la même conversation via {@code replyTo} (modèle « réponse » Discord, sans imbrication d'arbre).</p>
 *
 * <p>Événements : {@link MessagePosted}.</p>
 */
public final class Message extends AggregateRoot {

    private final MessageId id;
    private final ConversationId conversationId;
    private final UUID authorId;
    private final MessageContent content;
    private final MessageId replyTo;
    private final Instant sentAt;

    private Message(
            MessageId id,
            ConversationId conversationId,
            UUID authorId,
            MessageContent content,
            MessageId replyTo,
            Instant sentAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId must not be null");
        this.authorId = Objects.requireNonNull(authorId, "authorId must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.replyTo = replyTo;
        this.sentAt = Objects.requireNonNull(sentAt, "sentAt must not be null");
    }

    /**
     * Poste un message dans une conversation, éventuellement en réponse à un autre.
     *
     * <p>Émet {@link MessagePosted}.</p>
     *
     * @param authorId l'identifiant du membre auteur (référence cross-BC)
     * @param replyTo  le message auquel celui-ci répond, ou {@code null} pour un message racine
     * @param now      instant de l'opération
     * @return le message créé
     */
    public static Message post(
            ConversationId conversationId,
            UUID authorId,
            MessageContent content,
            MessageId replyTo,
            Instant now
    ) {
        Message message = new Message(MessageId.generate(), conversationId, authorId, content, replyTo, now);
        message.registerEvent(new MessagePosted(message.id, conversationId, authorId, now));
        return message;
    }

    /**
     * Reconstruit un message existant depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     *
     * @return le message reconstitué
     */
    public static Message reconstitute(
            MessageId id,
            ConversationId conversationId,
            UUID authorId,
            MessageContent content,
            MessageId replyTo,
            Instant sentAt
    ) {
        return new Message(id, conversationId, authorId, content, replyTo, sentAt);
    }

    /**
     * Indique si un message appartient à la conversation donnée.
     */
    public boolean belongsTo(ConversationId conversationId) {
        return this.conversationId.equals(conversationId);
    }

    public MessageId id() {
        return id;
    }

    public ConversationId conversationId() {
        return conversationId;
    }

    public UUID authorId() {
        return authorId;
    }

    public MessageContent content() {
        return content;
    }

    public MessageId replyTo() {
        return replyTo;
    }

    public Instant sentAt() {
        return sentAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message that = (Message) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", authorId=" + authorId +
                ", content=" + content +
                ", replyTo=" + replyTo +
                ", sentAt=" + sentAt +
                '}';
    }
}
