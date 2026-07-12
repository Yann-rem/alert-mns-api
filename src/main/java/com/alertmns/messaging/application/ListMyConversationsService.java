package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.port.incoming.ListMyConversationsUseCase;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.organisation.application.CurrentMemberResolver;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service applicatif listant les conversations de l'utilisateur courant.
 *
 * <p>Resolve (utilisateur courant → Member) → charge ses conversations directes (DM où il est dans la paire) et
 * celles des groupes dont il est membre (via l'ACL {@link GroupMembershipPort}) → fusionne et trie de la plus
 * récente à la plus ancienne. Pas de contrôle d'accès explicite : par construction, seules les conversations dont
 * le membre fait partie sont retournées.</p>
 */
public final class ListMyConversationsService implements ListMyConversationsUseCase {

    private final CurrentMemberResolver currentMemberResolver;
    private final ConversationRepository conversationRepository;
    private final GroupMembershipPort groupMembershipPort;

    public ListMyConversationsService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            GroupMembershipPort groupMembershipPort
    ) {
        this.currentMemberResolver = Objects.requireNonNull(
                currentMemberResolver, "currentMemberResolver must not be null");
        this.conversationRepository = Objects.requireNonNull(
                conversationRepository, "conversationRepository must not be null");
        this.groupMembershipPort = Objects.requireNonNull(
                groupMembershipPort, "groupMembershipPort must not be null");
    }

    @Override
    public List<Conversation> list() {
        UUID memberId = currentMemberResolver.resolveCurrentMember().id().value();

        List<Conversation> directConversations = conversationRepository.findByParticipant(memberId);

        List<UUID> groupIds = groupMembershipPort.groupIdsOf(memberId);
        List<Conversation> groupConversations = groupIds.isEmpty()
                ? List.of()
                : conversationRepository.findByGroupIdIn(groupIds);

        return Stream.concat(directConversations.stream(), groupConversations.stream())
                .sorted(Comparator.comparing(Conversation::createdAt).reversed())
                .toList();
    }
}
