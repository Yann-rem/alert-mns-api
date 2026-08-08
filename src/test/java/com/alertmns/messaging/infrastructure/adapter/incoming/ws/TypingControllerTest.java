package com.alertmns.messaging.infrastructure.adapter.incoming.ws;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.MemberDirectoryPort;
import com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("TypingController")
@ExtendWith(MockitoExtension.class)
class TypingControllerTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    MemberDirectoryPort memberDirectory;

    @Mock
    UserDirectoryPort userDirectory;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    TypingController controller;

    UUID typistUserId = UUID.randomUUID();
    ConversationId conversationId = ConversationId.generate();
    Principal typist = () -> typistUserId.toString();

    private Conversation directConversation() {
        return Conversation.reconstitute(
                conversationId, ORGANISATION_ID, null, null, ConversationKind.DIRECT,
                ParticipantPair.of(UUID.randomUUID(), UUID.randomUUID()), NOW);
    }

    private Conversation groupConversation(UUID groupId) {
        return Conversation.reconstitute(
                conversationId, ORGANISATION_ID, groupId, ConversationName.of("Général"),
                ConversationKind.GROUP, null, NOW);
    }

    @Test
    @DisplayName("DIRECT: relays the typing signal to the other participant only")
    void shouldRelayToOtherParticipant() {
        UUID other = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversation()));
        when(memberDirectory.directRecipients(any(), any())).thenReturn(List.of(typistUserId, other));
        when(userDirectory.displayName(typistUserId)).thenReturn("Sofia Nkolo");

        controller.typing(conversationId.value().toString(), typist);

        ArgumentCaptor<TypingNotification> captor = ArgumentCaptor.forClass(TypingNotification.class);
        verify(messagingTemplate).convertAndSendToUser(eq(other.toString()), eq("/queue/typing"), captor.capture());
        assertEquals(conversationId.value(), captor.getValue().conversationId());
        assertEquals(typistUserId, captor.getValue().userId());
        assertEquals("Sofia Nkolo", captor.getValue().userName());
        verify(messagingTemplate, never()).convertAndSendToUser(eq(typistUserId.toString()), anyString(), any());
    }

    @Test
    @DisplayName("GROUP: relays to every group member except the typist")
    void shouldRelayToGroupMembersExceptTypist() {
        UUID groupId = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        UUID u3 = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(groupConversation(groupId)));
        when(memberDirectory.groupRecipients(groupId)).thenReturn(List.of(typistUserId, u2, u3));

        controller.typing(conversationId.value().toString(), typist);

        verify(messagingTemplate).convertAndSendToUser(eq(u2.toString()), eq("/queue/typing"), any());
        verify(messagingTemplate).convertAndSendToUser(eq(u3.toString()), eq("/queue/typing"), any());
        verify(messagingTemplate, never()).convertAndSendToUser(eq(typistUserId.toString()), anyString(), any());
        verify(memberDirectory, never()).directRecipients(any(), any());
    }

    @Test
    @DisplayName("ignores a typist who is not a participant of the conversation")
    void shouldIgnoreNonParticipant() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversation()));
        when(memberDirectory.directRecipients(any(), any()))
                .thenReturn(List.of(UUID.randomUUID(), UUID.randomUUID()));

        controller.typing(conversationId.value().toString(), typist);

        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("ignores a typing signal for an unknown conversation")
    void shouldIgnoreUnknownConversation() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        controller.typing(conversationId.value().toString(), typist);

        verifyNoInteractions(memberDirectory);
        verifyNoInteractions(messagingTemplate);
    }
}
