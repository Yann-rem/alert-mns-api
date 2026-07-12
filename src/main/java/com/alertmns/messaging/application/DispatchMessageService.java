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
 * <p>C'est ici — dans l'application — qu'est composée la chaîne member → user → nom : chaque port ACL reste mono-BC
 * ({@link MemberDirectoryPort} vers Organisation, {@link UserDirectoryPort} vers Identity).</p>
 */
public final class DispatchMessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MemberDirectoryPort memberDirectory;
    private final UserDirectoryPort userDirectory;
    private final MessageRealtimePort realtimePort;

    public DispatchMessageService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            MemberDirectoryPort memberDirectory,
            UserDirectoryPort userDirectory,
            MessageRealtimePort realtimePort
    ) {
        this.conversationRepository = Objects.requireNonNull(
                conversationRepository, "conversationRepository must not be null");
        this.messageRepository = Objects.requireNonNull(messageRepository, "messageRepository must not be null");
        this.memberDirectory = Objects.requireNonNull(memberDirectory, "memberDirectory must not be null");
        this.userDirectory = Objects.requireNonNull(userDirectory, "userDirectory must not be null");
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
                resolveAuthorName(authorMemberId),
                message.content().value(),
                replyTo == null ? null : replyTo.value(),
                message.sentAt()
        );
    }

    private String resolveAuthorName(UUID authorMemberId) {
        return memberDirectory.userIdOf(authorMemberId)
                .map(userDirectory::displayName)
                .orElse(UserDirectoryPort.DELETED_USER_DISPLAY_NAME);
    }
}
