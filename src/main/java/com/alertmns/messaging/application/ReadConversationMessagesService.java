package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.exception.ConversationNotFoundException;
import com.alertmns.messaging.domain.exception.NotAConversationParticipantException;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.port.incoming.ReadConversationMessagesUseCase;
import com.alertmns.messaging.domain.port.incoming.command.ReadConversationMessagesQuery;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipChecker;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.organisation.application.CurrentMemberResolver;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service applicatif orchestrant la lecture paginée des messages d'une conversation.
 *
 * <p>Resolve (utilisateur courant → Member) → load (conversation) → check (le lecteur participe à la conversation,
 * l'appartenance aux groupes étant vérifiée via l'ACL GroupMembershipChecker) → read (messages paginés, plus récents
 * d'abord).</p>
 */
public final class ReadConversationMessagesService implements ReadConversationMessagesUseCase {

    private final CurrentMemberResolver currentMemberResolver;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final GroupMembershipChecker groupMembershipChecker;

    public ReadConversationMessagesService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            GroupMembershipChecker groupMembershipChecker
    ) {
        this.currentMemberResolver = Objects.requireNonNull(
                currentMemberResolver, "currentMemberResolver must not be null");
        this.conversationRepository = Objects.requireNonNull(
                conversationRepository, "conversationRepository must not be null");
        this.messageRepository = Objects.requireNonNull(messageRepository, "messageRepository must not be null");
        this.groupMembershipChecker = Objects.requireNonNull(
                groupMembershipChecker, "groupMembershipChecker must not be null");
    }

    @Override
    public List<Message> read(ReadConversationMessagesQuery query) {
        UUID memberId = currentMemberResolver.resolveCurrentMember().id().value();

        ConversationId conversationId = ConversationId.from(query.conversationId());
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        if (!conversation.isParticipant(memberId, groupMembershipChecker)) {
            throw new NotAConversationParticipantException(conversationId, memberId);
        }

        return messageRepository.findByConversationId(conversationId, query.page(), query.size());
    }
}
