package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.event.MessagePosted;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageContent;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.MemberDirectoryPort;
import com.alertmns.messaging.domain.port.outgoing.MessageNotification;
import com.alertmns.messaging.domain.port.outgoing.MessageRealtimePort;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DispatchMessageService")
@ExtendWith(MockitoExtension.class)
class DispatchMessageServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    MessageRepository messageRepository;

    @Mock
    MemberDirectoryPort memberDirectory;

    @Mock
    MemberNameResolver nameResolver;

    @Mock
    MessageRealtimePort realtimePort;

    @InjectMocks
    DispatchMessageService service;

    ConversationId conversationId = ConversationId.generate();
    MessageId messageId = MessageId.generate();
    UUID authorMemberId = UUID.randomUUID();

    private MessagePosted event() {
        return new MessagePosted(messageId, conversationId, authorMemberId, NOW);
    }

    private Message message(String content) {
        return Message.reconstitute(messageId, conversationId, authorMemberId, MessageContent.of(content), null, NOW);
    }

    private Conversation directConversation(UUID otherMemberId) {
        return Conversation.reconstitute(
                conversationId, ORGANISATION_ID, null, null,
                ConversationKind.DIRECT, ParticipantPair.of(authorMemberId, otherMemberId), NOW);
    }

    private Conversation groupConversation(UUID groupId) {
        return Conversation.reconstitute(
                conversationId, ORGANISATION_ID, groupId, ConversationName.of("Général"),
                ConversationKind.GROUP, null, NOW);
    }

    @Nested
    @DisplayName("Dispatching")
    class Dispatching {

        @Test
        @DisplayName("DIRECT: pushes to both participants with the resolved author name and content")
        void shouldPushToDirectParticipants() {
            UUID otherMemberId = UUID.randomUUID();
            Conversation conversation = directConversation(otherMemberId);
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(message("Bonjour")));
            when(memberDirectory.directRecipients(conversation.participantPair().low(),
                    conversation.participantPair().high())).thenReturn(List.of(u1, u2));
            when(nameResolver.nameOf(authorMemberId)).thenReturn("Alice Martin");

            service.dispatch(event());

            ArgumentCaptor<MessageNotification> captor = ArgumentCaptor.forClass(MessageNotification.class);
            verify(realtimePort).push(eq(List.of(u1, u2)), captor.capture());
            MessageNotification notification = captor.getValue();
            assertEquals(messageId.value(), notification.messageId());
            assertEquals(conversationId.value(), notification.conversationId());
            assertEquals(authorMemberId, notification.authorId());
            assertEquals("Alice Martin", notification.authorName());
            assertEquals("Bonjour", notification.content());
            assertNull(notification.replyToId());
            assertEquals(NOW, notification.sentAt());
        }

        @Test
        @DisplayName("GROUP: pushes to the group members, not the direct pair")
        void shouldPushToGroupMembers() {
            UUID groupId = UUID.randomUUID();
            UUID u1 = UUID.randomUUID();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(groupConversation(groupId)));
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(message("Salut le groupe")));
            when(memberDirectory.groupRecipients(groupId)).thenReturn(List.of(u1));
            when(nameResolver.nameOf(authorMemberId)).thenReturn("Bob");

            service.dispatch(event());

            verify(realtimePort).push(eq(List.of(u1)), any(MessageNotification.class));
            verify(memberDirectory, never()).directRecipients(any(), any());
        }

        /**
         * Le repli sur « Utilisateur supprimé » appartient désormais à {@link MemberNameResolver} et
         * y est testé ; ici on vérifie seulement qu'il est propagé tel quel dans la notification.
         */
        @Test
        @DisplayName("propagates the placeholder name resolved for an anonymised or orphan author")
        void shouldPropagatePlaceholderForOrphanAuthor() {
            UUID otherMemberId = UUID.randomUUID();
            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(directConversation(otherMemberId)));
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(message("Coucou")));
            when(memberDirectory.directRecipients(any(), any())).thenReturn(List.of(UUID.randomUUID()));
            when(nameResolver.nameOf(authorMemberId))
                    .thenReturn(UserDirectoryPort.DELETED_USER_DISPLAY_NAME);

            service.dispatch(event());

            ArgumentCaptor<MessageNotification> captor = ArgumentCaptor.forClass(MessageNotification.class);
            verify(realtimePort).push(any(), captor.capture());
            assertEquals(UserDirectoryPort.DELETED_USER_DISPLAY_NAME, captor.getValue().authorName());
        }

        @Test
        @DisplayName("does nothing when the conversation is gone")
        void shouldNotPushWhenConversationAbsent() {
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

            service.dispatch(event());

            verify(messageRepository, never()).findById(any());
            verify(realtimePort, never()).push(any(), any());
        }

        @Test
        @DisplayName("does nothing when the message is gone")
        void shouldNotPushWhenMessageAbsent() {
            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(directConversation(UUID.randomUUID())));
            when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

            service.dispatch(event());

            verify(realtimePort, never()).push(any(), any());
        }

        @Test
        @DisplayName("does nothing when the audience resolves to no recipient")
        void shouldNotPushWhenNoRecipient() {
            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(directConversation(UUID.randomUUID())));
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(message("Personne")));
            when(memberDirectory.directRecipients(any(), any())).thenReturn(List.of());

            service.dispatch(event());

            verify(realtimePort, never()).push(any(), any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null collaborators")
        void shouldRejectNullCollaborators() {
            assertThrows(NullPointerException.class, () -> new DispatchMessageService(
                    null, messageRepository, memberDirectory, nameResolver, realtimePort));
            assertThrows(NullPointerException.class, () -> new DispatchMessageService(
                    conversationRepository, null, memberDirectory, nameResolver, realtimePort));
            assertThrows(NullPointerException.class, () -> new DispatchMessageService(
                    conversationRepository, messageRepository, null, nameResolver, realtimePort));
            assertThrows(NullPointerException.class, () -> new DispatchMessageService(
                    conversationRepository, messageRepository, memberDirectory, null, realtimePort));
            assertThrows(NullPointerException.class, () -> new DispatchMessageService(
                    conversationRepository, messageRepository, memberDirectory, nameResolver, null));
        }
    }
}
