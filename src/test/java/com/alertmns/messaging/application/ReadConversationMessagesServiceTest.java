package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.exception.ConversationNotFoundException;
import com.alertmns.messaging.domain.exception.NotAConversationParticipantException;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageContent;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.command.ReadConversationMessagesQuery;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipChecker;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ReadConversationMessagesService")
@ExtendWith(MockitoExtension.class)
class ReadConversationMessagesServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    CurrentMemberResolver currentMemberResolver;

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    MessageRepository messageRepository;

    @Mock
    GroupMembershipChecker groupMembershipChecker;

    @InjectMocks
    ReadConversationMessagesService service;

    MemberId readerMemberId;
    Member reader;
    ConversationId conversationId;

    @BeforeEach
    void setUp() {
        readerMemberId = MemberId.generate();
        reader = Member.reconstitute(
                readerMemberId, ORGANISATION_ID, UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW);
        conversationId = ConversationId.generate();
    }

    private void stubAuthenticatedReader() {
        when(currentMemberResolver.resolveCurrentMember()).thenReturn(reader);
    }

    private ReadConversationMessagesQuery query() {
        return new ReadConversationMessagesQuery(conversationId.value().toString(), 0, 50);
    }

    private Conversation directConversationWithReader() {
        ParticipantPair pair = ParticipantPair.of(readerMemberId.value(), UUID.randomUUID());
        return Conversation.reconstitute(
                conversationId, ORGANISATION_ID, null, null, ConversationKind.DIRECT, pair, NOW);
    }

    private Conversation groupConversation(UUID groupId) {
        return Conversation.reconstitute(
                conversationId, ORGANISATION_ID, groupId, ConversationName.of("Général"), ConversationKind.GROUP,
                null, NOW);
    }

    @Nested
    @DisplayName("Reading")
    class Reading {

        @Test
        @DisplayName("should return the messages of a DIRECT conversation the reader belongs to")
        void shouldReturnMessagesDirect() {
            stubAuthenticatedReader();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversationWithReader()));
            Message message = Message.reconstitute(
                    MessageId.generate(), conversationId, readerMemberId.value(), MessageContent.of("A"), null, NOW);
            when(messageRepository.findByConversationId(eq(conversationId), anyInt(), anyInt()))
                    .thenReturn(List.of(message));

            List<Message> result = service.read(query());

            assertEquals(List.of(message), result);
        }

        @Test
        @DisplayName("should return messages of a GROUP conversation when the reader is a member of the group")
        void shouldReturnMessagesGroup() {
            stubAuthenticatedReader();
            UUID groupId = UUID.randomUUID();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(groupConversation(groupId)));
            when(groupMembershipChecker.isMember(groupId, readerMemberId.value())).thenReturn(true);
            when(messageRepository.findByConversationId(eq(conversationId), anyInt(), anyInt()))
                    .thenReturn(List.of());

            service.read(query());

            verify(messageRepository).findByConversationId(eq(conversationId), anyInt(), anyInt());
        }

        @Test
        @DisplayName("should propagate the requested page and size to the repository")
        void shouldPropagatePagination() {
            stubAuthenticatedReader();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversationWithReader()));
            when(messageRepository.findByConversationId(conversationId, 2, 25)).thenReturn(List.of());

            service.read(new ReadConversationMessagesQuery(conversationId.value().toString(), 2, 25));

            verify(messageRepository).findByConversationId(conversationId, 2, 25);
        }
    }

    @Nested
    @DisplayName("Access")
    class Access {

        @Test
        @DisplayName("should throw ConversationNotFoundException when the conversation does not exist")
        void shouldThrowWhenConversationAbsent() {
            stubAuthenticatedReader();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

            assertThrows(ConversationNotFoundException.class, () -> service.read(query()));
            verify(messageRepository, never()).findByConversationId(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("DIRECT: should throw NotAConversationParticipantException when the reader is outside the pair")
        void shouldThrowWhenNotInPair() {
            stubAuthenticatedReader();
            ParticipantPair otherPair = ParticipantPair.of(UUID.randomUUID(), UUID.randomUUID());
            Conversation conversation = Conversation.reconstitute(
                    conversationId, ORGANISATION_ID, null, null, ConversationKind.DIRECT, otherPair, NOW);
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

            assertThrows(NotAConversationParticipantException.class, () -> service.read(query()));
            verify(messageRepository, never()).findByConversationId(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("GROUP: should throw NotAConversationParticipantException when the reader is not in the group")
        void shouldThrowWhenNotGroupMember() {
            stubAuthenticatedReader();
            UUID groupId = UUID.randomUUID();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(groupConversation(groupId)));
            when(groupMembershipChecker.isMember(groupId, readerMemberId.value())).thenReturn(false);

            assertThrows(NotAConversationParticipantException.class, () -> service.read(query()));
            verify(messageRepository, never()).findByConversationId(any(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null currentMemberResolver")
        void shouldRejectNullCurrentMemberResolver() {
            assertThrows(NullPointerException.class, () -> new ReadConversationMessagesService(
                    null, conversationRepository, messageRepository, groupMembershipChecker));
        }

        @Test
        @DisplayName("should reject null conversationRepository")
        void shouldRejectNullConversationRepository() {
            assertThrows(NullPointerException.class, () -> new ReadConversationMessagesService(
                    currentMemberResolver, null, messageRepository, groupMembershipChecker));
        }

        @Test
        @DisplayName("should reject null messageRepository")
        void shouldRejectNullMessageRepository() {
            assertThrows(NullPointerException.class, () -> new ReadConversationMessagesService(
                    currentMemberResolver, conversationRepository, null, groupMembershipChecker));
        }

        @Test
        @DisplayName("should reject null groupMembershipChecker")
        void shouldRejectNullGroupMembershipChecker() {
            assertThrows(NullPointerException.class, () -> new ReadConversationMessagesService(
                    currentMemberResolver, conversationRepository, messageRepository, null));
        }

        @Test
        @DisplayName("should reject null query conversationId")
        void shouldRejectNullQueryConversationId() {
            assertThrows(NullPointerException.class, () -> new ReadConversationMessagesQuery(null, 0, 50));
        }

        @Test
        @DisplayName("should reject a negative page")
        void shouldRejectNegativePage() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ReadConversationMessagesQuery(conversationId.value().toString(), -1, 50));
        }

        @Test
        @DisplayName("should reject a size below 1 or above 100")
        void shouldRejectOutOfBoundsSize() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ReadConversationMessagesQuery(conversationId.value().toString(), 0, 0));
            assertThrows(IllegalArgumentException.class,
                    () -> new ReadConversationMessagesQuery(conversationId.value().toString(), 0, 101));
        }
    }
}
