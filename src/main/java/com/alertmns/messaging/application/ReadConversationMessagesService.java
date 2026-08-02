package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.exception.ConversationNotFoundException;
import com.alertmns.messaging.domain.exception.NotAConversationParticipantException;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.port.incoming.MessageView;
import com.alertmns.messaging.domain.port.incoming.ReadConversationMessagesUseCase;
import com.alertmns.messaging.domain.port.incoming.command.ReadConversationMessagesQuery;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipChecker;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.organisation.application.CurrentMemberResolver;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Service applicatif orchestrant la lecture paginée des messages d'une conversation.
 *
 * <p>Resolve (utilisateur courant → Member) → load (conversation) → check (le lecteur participe à la conversation,
 * l'appartenance aux groupes étant vérifiée via l'ACL GroupMembershipChecker) → read (messages paginés, plus récents
 * d'abord) → enrichit du nom des auteurs.</p>
 */
public final class ReadConversationMessagesService implements ReadConversationMessagesUseCase {

    private final CurrentMemberResolver currentMemberResolver;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final GroupMembershipChecker groupMembershipChecker;
    private final MemberNameResolver nameResolver;

    public ReadConversationMessagesService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            GroupMembershipChecker groupMembershipChecker,
            MemberNameResolver nameResolver
    ) {
        this.currentMemberResolver = Objects.requireNonNull(
                currentMemberResolver, "currentMemberResolver must not be null");
        this.conversationRepository = Objects.requireNonNull(
                conversationRepository, "conversationRepository must not be null");
        this.messageRepository = Objects.requireNonNull(messageRepository, "messageRepository must not be null");
        this.groupMembershipChecker = Objects.requireNonNull(
                groupMembershipChecker, "groupMembershipChecker must not be null");
        this.nameResolver = Objects.requireNonNull(nameResolver, "nameResolver must not be null");
    }

    @Override
    public List<MessageView> read(ReadConversationMessagesQuery query) {
        UUID memberId = currentMemberResolver.resolveCurrentMember().id().value();

        ConversationId conversationId = ConversationId.from(query.conversationId());
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        if (!conversation.isParticipant(memberId, groupMembershipChecker)) {
            throw new NotAConversationParticipantException(conversationId, memberId);
        }

        List<Message> messages = messageRepository.findByConversationId(
                conversationId, query.page(), query.size());

        // Le nom de l'auteur est résolu ici plutôt que laissé au client : lui seul connaît les
        // memberId, et il n'a aucun moyen de les traduire (l'annuaire est réservé aux ADMIN).
        Map<UUID, String> names = nameResolver.namesOf(messages.stream().map(Message::authorId).toList());

        return messages.stream()
                .map(message -> new MessageView(message, names.get(message.authorId())))
                .toList();
    }
}
