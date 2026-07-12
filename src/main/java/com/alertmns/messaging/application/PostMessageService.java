package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.exception.ConversationNotFoundException;
import com.alertmns.messaging.domain.exception.InvalidReplyTargetException;
import com.alertmns.messaging.domain.exception.NotAConversationParticipantException;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageContent;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.port.incoming.PostMessageUseCase;
import com.alertmns.messaging.domain.port.incoming.command.PostMessageCommand;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipChecker;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.shared.EventPublisher;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * Service applicatif orchestrant l'envoi d'un message dans une conversation.
 *
 * <p>Resolve (utilisateur courant → Member auteur) → load (conversation) → check (l'auteur participe à la
 * conversation, l'appartenance aux groupes étant vérifiée via l'ACL GroupMembershipChecker) → resolve (cible de la
 * réponse, si replyTo) → act (Message.post) → save → publish.</p>
 */
public final class PostMessageService implements PostMessageUseCase {

    private final CurrentMemberResolver currentMemberResolver;
    private final ConversationRepository conversationRepository;
    private final GroupMembershipChecker groupMembershipChecker;
    private final MessageRepository messageRepository;
    private final EventPublisher publisher;
    private final Clock clock;

    public PostMessageService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            GroupMembershipChecker groupMembershipChecker,
            MessageRepository messageRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        this.currentMemberResolver = Objects.requireNonNull(
                currentMemberResolver, "currentMemberResolver must not be null");
        this.conversationRepository = Objects.requireNonNull(
                conversationRepository, "conversationRepository must not be null");
        this.groupMembershipChecker = Objects.requireNonNull(
                groupMembershipChecker, "groupMembershipChecker must not be null");
        this.messageRepository = Objects.requireNonNull(
                messageRepository, "messageRepository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public MessageId post(PostMessageCommand command) {
        UUID authorId = currentMemberResolver.resolveCurrentMember().id().value();

        ConversationId conversationId = ConversationId.from(command.conversationId());
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        if (!conversation.isParticipant(authorId, groupMembershipChecker)) {
            throw new NotAConversationParticipantException(conversationId, authorId);
        }

        MessageId replyTo = resolveReplyTo(command, conversation);

        MessageContent content = MessageContent.of(command.content());
        Message message = Message.post(conversationId, authorId, content, replyTo, clock.instant());
        messageRepository.save(message);
        publisher.publish(message.pullDomainEvents());
        return message.id();
    }

    private MessageId resolveReplyTo(PostMessageCommand command, Conversation conversation) {
        String replyToMessageId = command.replyToMessageId();
        if (replyToMessageId == null) {
            return null;
        }
        MessageId replyTo = MessageId.from(replyToMessageId);
        Message target = messageRepository.findById(replyTo)
                .orElseThrow(() -> InvalidReplyTargetException.notFound(replyTo));
        if (!target.belongsTo(conversation.id())) {
            throw InvalidReplyTargetException.notInConversation(conversation.id(), replyTo);
        }
        return replyTo;
    }
}
