package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.ConversationSummary;
import com.alertmns.messaging.domain.port.incoming.ConversationSummary.LastMessage;
import com.alertmns.messaging.domain.port.incoming.ListMyConversationsUseCase;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort;
import com.alertmns.organisation.application.CurrentMemberResolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service applicatif listant les conversations de l'utilisateur courant.
 *
 * <p>Resolve (utilisateur courant → Member) → charge ses conversations directes (DM où il est dans la paire) et
 * celles des groupes dont il est membre (via l'ACL {@link GroupMembershipPort}) → fusionne, enrichit et trie. Pas de
 * contrôle d'accès explicite : par construction, seules les conversations dont le membre fait partie sont
 * retournées.</p>
 *
 * <p><b>Enrichissement</b> : un DM n'a pas de nom en base, seulement une paire de {@code memberId}. Son titre est donc
 * résolu pour le lecteur — c'est le nom de l'<i>autre</i> participant — ce qui n'a de sens qu'ici, où l'on sait qui
 * demande. Le dernier message est chargé en une requête pour toutes les conversations, jamais une par une.</p>
 *
 * <p><b>Tri</b> : par dernière activité réelle et non par date de création, sans quoi une conversation ancienne mais
 * vivante s'enfoncerait sous des conversations récentes et muettes.</p>
 */
public final class ListMyConversationsService implements ListMyConversationsUseCase {

    private final CurrentMemberResolver currentMemberResolver;
    private final ConversationRepository conversationRepository;
    private final GroupMembershipPort groupMembershipPort;
    private final MessageRepository messageRepository;
    private final MemberNameResolver nameResolver;

    public ListMyConversationsService(
            CurrentMemberResolver currentMemberResolver,
            ConversationRepository conversationRepository,
            GroupMembershipPort groupMembershipPort,
            MessageRepository messageRepository,
            MemberNameResolver nameResolver
    ) {
        this.currentMemberResolver = Objects.requireNonNull(
                currentMemberResolver, "currentMemberResolver must not be null");
        this.conversationRepository = Objects.requireNonNull(
                conversationRepository, "conversationRepository must not be null");
        this.groupMembershipPort = Objects.requireNonNull(
                groupMembershipPort, "groupMembershipPort must not be null");
        this.messageRepository = Objects.requireNonNull(messageRepository, "messageRepository must not be null");
        this.nameResolver = Objects.requireNonNull(nameResolver, "nameResolver must not be null");
    }

    @Override
    public List<ConversationSummary> list() {
        UUID readerId = currentMemberResolver.resolveCurrentMember().id().value();

        List<Conversation> conversations = loadConversations(readerId);
        if (conversations.isEmpty()) {
            return List.of();
        }

        Map<ConversationId, Message> lastMessages = messageRepository.findLastMessagePerConversation(
                conversations.stream().map(Conversation::id).toList());
        Map<UUID, String> names = nameResolver.namesOf(namesToResolve(conversations, lastMessages, readerId));

        return conversations.stream()
                .map(conversation -> toSummary(conversation, lastMessages, names, readerId))
                .sorted(Comparator.comparing(ConversationSummary::lastActivityAt).reversed())
                .toList();
    }

    private List<Conversation> loadConversations(UUID memberId) {
        List<Conversation> direct = conversationRepository.findByParticipant(memberId);

        List<UUID> groupIds = groupMembershipPort.groupIdsOf(memberId);
        List<Conversation> groups = groupIds.isEmpty()
                ? List.of()
                : conversationRepository.findByGroupIdIn(groupIds);

        return Stream.concat(direct.stream(), groups.stream()).toList();
    }

    /** Interlocuteurs des DM et auteurs des derniers messages : tout ce qu'il faudra nommer. */
    private List<UUID> namesToResolve(
            List<Conversation> conversations,
            Map<ConversationId, Message> lastMessages,
            UUID readerId
    ) {
        List<UUID> memberIds = new ArrayList<>();
        for (Conversation conversation : conversations) {
            UUID counterpart = counterpartOf(conversation, readerId);
            if (counterpart != null) {
                memberIds.add(counterpart);
            }
            Message last = lastMessages.get(conversation.id());
            if (last != null) {
                memberIds.add(last.authorId());
            }
        }
        return memberIds;
    }

    /** @return l'autre participant d'un DM, ou {@code null} s'il s'agit d'un groupe */
    private static UUID counterpartOf(Conversation conversation, UUID readerId) {
        ParticipantPair pair = conversation.participantPair();
        if (pair == null) {
            return null;
        }
        return readerId.equals(pair.low()) ? pair.high() : pair.low();
    }

    private ConversationSummary toSummary(
            Conversation conversation,
            Map<ConversationId, Message> lastMessages,
            Map<UUID, String> names,
            UUID readerId
    ) {
        UUID counterpart = counterpartOf(conversation, readerId);
        ConversationName name = conversation.name();
        // Un nom manquant dégrade en « Utilisateur supprimé » plutôt que de faire échouer toute la
        // liste : un seul membre disparu ne doit pas priver l'utilisateur de sa messagerie.
        String title = counterpart != null ? nameOrPlaceholder(names, counterpart) : groupName(name);

        Message last = lastMessages.get(conversation.id());
        LastMessage preview = last == null ? null : new LastMessage(
                last.id().value(),
                last.authorId(),
                nameOrPlaceholder(names, last.authorId()),
                last.content().value(),
                last.sentAt());

        return new ConversationSummary(conversation, title, counterpart, preview);
    }

    private static String nameOrPlaceholder(Map<UUID, String> names, UUID memberId) {
        return names.getOrDefault(memberId, UserDirectoryPort.DELETED_USER_DISPLAY_NAME);
    }

    /** Une conversation de groupe porte toujours un nom ; la garde couvre une donnée corrompue. */
    private static String groupName(ConversationName name) {
        return name == null ? "" : name.value();
    }
}
