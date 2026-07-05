package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipPort;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        @DisplayName("should return DM and group conversations sorted from most recent to oldest")
        void shouldReturnSortedConversations() {
            stubCurrentMember();
            Conversation oldestDm = directConversation(NOW);
            Conversation middleDm = directConversation(NOW.plusSeconds(60));
            Conversation newestGroup = groupConversation(UUID.randomUUID(), NOW.plusSeconds(120));
            when(conversationRepository.findByParticipant(memberId.value())).thenReturn(List.of(oldestDm, middleDm));
            UUID groupId = UUID.randomUUID();
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of(groupId));
            when(conversationRepository.findByGroupIdIn(List.of(groupId))).thenReturn(List.of(newestGroup));

            List<Conversation> result = service.list();

            assertEquals(List.of(newestGroup, middleDm, oldestDm), result);
        }

        @Test
        @DisplayName("should return only DM conversations and not query groups when the member has no group")
        void shouldReturnOnlyDmWhenNoGroup() {
            stubCurrentMember();
            Conversation dm = directConversation(NOW);
            when(conversationRepository.findByParticipant(memberId.value())).thenReturn(List.of(dm));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of());

            List<Conversation> result = service.list();

            assertEquals(List.of(dm), result);
            verify(conversationRepository, never()).findByGroupIdIn(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null currentMemberResolver")
        void shouldRejectNullCurrentMemberResolver() {
            assertThrows(NullPointerException.class,
                    () -> new ListMyConversationsService(null, conversationRepository, groupMembershipPort));
        }

        @Test
        @DisplayName("should reject null conversationRepository")
        void shouldRejectNullConversationRepository() {
            assertThrows(NullPointerException.class,
                    () -> new ListMyConversationsService(currentMemberResolver, null, groupMembershipPort));
        }

        @Test
        @DisplayName("should reject null groupMembershipPort")
        void shouldRejectNullGroupMembershipPort() {
            assertThrows(NullPointerException.class,
                    () -> new ListMyConversationsService(currentMemberResolver, conversationRepository, null));
        }
    }
}
