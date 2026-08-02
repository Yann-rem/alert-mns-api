package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.event.MessagePosted;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.MemberDirectoryPort;
import com.alertmns.messaging.domain.port.outgoing.MessageNotification;
import com.alertmns.messaging.domain.port.outgoing.MessageRealtimePort;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service applicatif livrant un message posté aux participants de sa conversation en temps réel.
 *
 * <p>Réaction à l'événement {@link MessagePosted} (thin) : recharge la conversation et le message, résout les
 * destinataires (paire directe ou membres du groupe → {@code userId}), résout le nom de l'auteur
 * (memberId → userId → nom d'affichage, anonymisation-aware), construit la projection {@link MessageNotification} et la
 * pousse.</p>
 *
 * <p>La chaîne member → user → nom est déléguée à {@link MemberNameResolver}, partagée avec les services de
 * lecture : le nom poussé en temps réel et celui rendu à la relecture de l'historique doivent être résolus par le
 * même chemin, sans quoi un membre anonymisé apparaîtrait différemment selon la voie empruntée.</p>
 */
public final class DispatchMessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MemberDirectoryPort memberDirectory;
    private final MemberNameResolver nameResolver;
    private final MessageRealtimePort realtimePort;

    public DispatchMessageService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            MemberDirectoryPort memberDirectory,
            MemberNameResolver nameResolver,
            MessageRealtimePort realtimePort
    ) {
        this.conversationRepository = Objects.requireNonNull(
                conversationRepository, "conversationRepository must not be null");
        this.messageRepository = Objects.requireNonNull(messageRepository, "messageRepository must not be null");
        this.memberDirectory = Objects.requireNonNull(memberDirectory, "memberDirectory must not be null");
        this.nameResolver = Objects.requireNonNull(nameResolver, "nameResolver must not be null");
        this.realtimePort = Objects.requireNonNull(realtimePort, "realtimePort must not be null");
    }

    public void dispatch(MessagePosted event) {
        Conversation conversation = conversationRepository.findById(event.conversationId()).orElse(null);
        if (conversation == null) {
            return;
        }
        Message message = messageRepository.findById(event.messageId()).orElse(null);
        if (message == null) {
            return;
        }

        List<UUID> recipients = resolveRecipients(conversation);
        if (recipients.isEmpty()) {
            return;
        }

        realtimePort.push(recipients, toNotification(conversation, message, event.authorId()));
    }

    private List<UUID> resolveRecipients(Conversation conversation) {
        return switch (conversation.kind()) {
            case DIRECT -> memberDirectory.directRecipients(
                    conversation.participantPair().low(), conversation.participantPair().high());
            case GROUP -> memberDirectory.groupRecipients(conversation.groupId());
        };
    }

    private MessageNotification toNotification(Conversation conversation, Message message, UUID authorMemberId) {
        MessageId replyTo = message.replyTo();
        return new MessageNotification(
                message.id().value(),
                conversation.id().value(),
                authorMemberId,
                nameResolver.nameOf(authorMemberId),
                message.content().value(),
                replyTo == null ? null : replyTo.value(),
                message.sentAt()
        );
    }
}
