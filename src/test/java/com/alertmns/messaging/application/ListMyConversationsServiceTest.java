package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageContent;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.ConversationSummary;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ListMyConversationsService")
@ExtendWith(MockitoExtension.class)
class ListMyConversationsServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    CurrentMemberResolver currentMemberResolver;

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    GroupMembershipPort groupMembershipPort;

    @Mock
    MessageRepository messageRepository;

    @Mock
    MemberNameResolver nameResolver;

    @InjectMocks
    ListMyConversationsService service;

    MemberId memberId;
    Member member;

    @BeforeEach
    void setUp() {
        memberId = MemberId.generate();
        member = Member.reconstitute(
                memberId, ORGANISATION_ID, UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW);
    }

    private void stubCurrentMember() {
        when(currentMemberResolver.resolveCurrentMember()).thenReturn(member);
    }

    private Conversation directConversation(Instant createdAt) {
        ParticipantPair pair = ParticipantPair.of(memberId.value(), UUID.randomUUID());
        return Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, null, null, ConversationKind.DIRECT, pair, createdAt);
    }

    private Conversation groupConversation(UUID groupId, Instant createdAt) {
        return Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, groupId, ConversationName.of("Général"),
                ConversationKind.GROUP, null, createdAt);
    }

    @Nested
    @DisplayName("Listing")
    class Listing {

        @Test
        @DisplayName("should return DM and group conversations sorted from most recent activity to oldest")
        void shouldReturnSortedConversations() {
            stubCurrentMember();
            Conversation oldestDm = directConversation(NOW);
            Conversation middleDm = directConversation(NOW.plusSeconds(60));
            Conversation newestGroup = groupConversation(UUID.randomUUID(), NOW.plusSeconds(120));
            when(conversationRepository.findByParticipant(memberId.value())).thenReturn(List.of(oldestDm, middleDm));
            UUID groupId = UUID.randomUUID();
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of(groupId));
            when(conversationRepository.findByGroupIdIn(List.of(groupId))).thenReturn(List.of(newestGroup));
            when(messageRepository.findLastMessagePerConversation(any())).thenReturn(Map.of());
            when(nameResolver.namesOf(any())).thenReturn(Map.of());

            List<ConversationSummary> result = service.list();

            assertEquals(
                    List.of(newestGroup, middleDm, oldestDm),
                    result.stream().map(ConversationSummary::conversation).toList());
        }

        /**
         * Le tri porte sur la dernière activité : une conversation ancienne mais vivante doit passer
         * devant une conversation récente et muette.
         */
        @Test
        @DisplayName("should sort by last message rather than by creation date")
        void shouldSortByLastActivity() {
            stubCurrentMember();
            Conversation oldButActive = directConversation(NOW);
            Conversation recentButSilent = directConversation(NOW.plusSeconds(60));
            when(conversationRepository.findByParticipant(memberId.value()))
                    .thenReturn(List.of(oldButActive, recentButSilent));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of());
            Message lastMessage = Message.reconstitute(
                    MessageId.generate(),
                    oldButActive.id(),
                    UUID.randomUUID(),
                    MessageContent.of("Toujours là"),
                    null,
                    NOW.plusSeconds(600));
            when(messageRepository.findLastMessagePerConversation(any()))
                    .thenReturn(Map.of(oldButActive.id(), lastMessage));
            when(nameResolver.namesOf(any())).thenReturn(Map.of());

            List<ConversationSummary> result = service.list();

            assertEquals(
                    List.of(oldButActive, recentButSilent),
                    result.stream().map(ConversationSummary::conversation).toList());
            assertEquals("Toujours là", result.getFirst().lastMessage().content());
        }

        @Test
        @DisplayName("should title a direct conversation with the counterpart name")
        void shouldTitleDirectConversationWithCounterpart() {
            stubCurrentMember();
            UUID counterpart = UUID.randomUUID();
            Conversation dm = Conversation.reconstitute(
                    ConversationId.generate(),
                    ORGANISATION_ID,
                    null,
                    null,
                    ConversationKind.DIRECT,
                    ParticipantPair.of(memberId.value(), counterpart),
                    NOW);
            when(conversationRepository.findByParticipant(memberId.value())).thenReturn(List.of(dm));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of());
            when(messageRepository.findLastMessagePerConversation(any())).thenReturn(Map.of());
            when(nameResolver.namesOf(any())).thenReturn(Map.of(counterpart, "Sofia Nkolo"));

            ConversationSummary summary = service.list().getFirst();

            assertEquals("Sofia Nkolo", summary.title());
            assertEquals(counterpart, summary.counterpartMemberId());
        }

        @Test
        @DisplayName("should title a group conversation with the group name")
        void shouldTitleGroupConversationWithItsName() {
            stubCurrentMember();
            UUID groupId = UUID.randomUUID();
            when(conversationRepository.findByParticipant(memberId.value())).thenReturn(List.of());
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of(groupId));
            when(conversationRepository.findByGroupIdIn(List.of(groupId)))
                    .thenReturn(List.of(groupConversation(groupId, NOW)));
            when(messageRepository.findLastMessagePerConversation(any())).thenReturn(Map.of());
            when(nameResolver.namesOf(any())).thenReturn(Map.of());

            ConversationSummary summary = service.list().getFirst();

            assertEquals("Général", summary.title());
            assertNull(summary.counterpartMemberId());
        }

        @Test
        @DisplayName("should return only DM conversations and not query groups when the member has no group")
        void shouldReturnOnlyDmWhenNoGroup() {
            stubCurrentMember();
            Conversation dm = directConversation(NOW);
            when(conversationRepository.findByParticipant(memberId.value())).thenReturn(List.of(dm));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of());
            when(messageRepository.findLastMessagePerConversation(any())).thenReturn(Map.of());
            when(nameResolver.namesOf(any())).thenReturn(Map.of());

            List<ConversationSummary> result = service.list();

            assertEquals(List.of(dm), result.stream().map(ConversationSummary::conversation).toList());
            verify(conversationRepository, never()).findByGroupIdIn(any());
        }

        /** Aucune conversation : inutile d'aller chercher des derniers messages ou des noms. */
        @Test
        @DisplayName("should not query messages nor names when there is no conversation")
        void shouldShortCircuitWhenNoConversation() {
            stubCurrentMember();
            when(conversationRepository.findByParticipant(memberId.value())).thenReturn(List.of());
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of());

            assertEquals(List.of(), service.list());
            verify(messageRepository, never()).findLastMessagePerConversation(any());
            verify(nameResolver, never()).namesOf(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null currentMemberResolver")
        void shouldRejectNullCurrentMemberResolver() {
            assertThrows(NullPointerException.class, () -> new ListMyConversationsService(
                    null, conversationRepository, groupMembershipPort, messageRepository, nameResolver));
        }

        @Test
        @DisplayName("should reject null conversationRepository")
        void shouldRejectNullConversationRepository() {
            assertThrows(NullPointerException.class, () -> new ListMyConversationsService(
                    currentMemberResolver, null, groupMembershipPort, messageRepository, nameResolver));
        }

        @Test
        @DisplayName("should reject null groupMembershipPort")
        void shouldRejectNullGroupMembershipPort() {
            assertThrows(NullPointerException.class, () -> new ListMyConversationsService(
                    currentMemberResolver, conversationRepository, null, messageRepository, nameResolver));
        }

        @Test
        @DisplayName("should reject null messageRepository")
        void shouldRejectNullMessageRepository() {
            assertThrows(NullPointerException.class, () -> new ListMyConversationsService(
                    currentMemberResolver, conversationRepository, groupMembershipPort, null, nameResolver));
        }

        @Test
        @DisplayName("should reject null nameResolver")
        void shouldRejectNullNameResolver() {
            assertThrows(NullPointerException.class, () -> new ListMyConversationsService(
                    currentMemberResolver, conversationRepository, groupMembershipPort, messageRepository, null));
        }
    }
}
