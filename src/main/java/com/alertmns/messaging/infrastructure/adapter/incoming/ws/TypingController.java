package com.alertmns.messaging.infrastructure.adapter.incoming.ws;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.MemberDirectoryPort;
import com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * Relais STOMP du signal « en train d'écrire » (typing indicator).
 *
 * <p>Premier usage bidirectionnel du socket : le client envoie un {@code SEND} sur
 * {@code /app/conversations/{conversationId}/typing} (corps vide), le serveur relaie « ce {@code userId} écrit » aux
 * <strong>autres</strong> participants sur leur file {@code /user/{userId}/queue/typing}.</p>
 *
 * <p><strong>Éphémère et 100 % infrastructure</strong> : aucun agrégat, aucun événement de domaine, aucune
 * persistance. Le contrôleur consomme les ports sortants existants ({@link ConversationRepository},
 * {@link MemberDirectoryPort}, {@link UserDirectoryPort}) comme des modèles de lecture, et pousse via
 * {@link SimpMessagingTemplate}. L'identité du typist provient du {@code Principal} posé au handshake
 * ({@code userId}). L'autorisation est implicite : un typist qui ne figure pas parmi les participants est ignoré.</p>
 *
 * <p>Le nom du typist est résolu ici, faute pour le client de savoir traduire un {@code userId} — il ne manipule que
 * des {@code memberId} (cf. {@link TypingNotification}).</p>
 */
@Controller
@RequiredArgsConstructor
public class TypingController {

    static final String DESTINATION = "/queue/typing";

    private final ConversationRepository conversationRepository;
    private final MemberDirectoryPort memberDirectory;
    private final UserDirectoryPort userDirectory;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/conversations/{conversationId}/typing")
    public void typing(@DestinationVariable String conversationId, Principal principal) {
        if (principal == null) {
            return;
        }
        UUID typistUserId = UUID.fromString(principal.getName());

        Conversation conversation = conversationRepository.findById(ConversationId.from(conversationId)).orElse(null);
        if (conversation == null) {
            return;
        }

        List<UUID> participants = resolveParticipants(conversation);
        if (!participants.contains(typistUserId)) {
            return;
        }

        TypingNotification notification = new TypingNotification(
                conversation.id().value(), typistUserId, userDirectory.displayName(typistUserId));
        for (UUID recipient : participants) {
            if (!recipient.equals(typistUserId)) {
                messagingTemplate.convertAndSendToUser(recipient.toString(), DESTINATION, notification);
            }
        }
    }

    private List<UUID> resolveParticipants(Conversation conversation) {
        return switch (conversation.kind()) {
            case DIRECT -> memberDirectory.directRecipients(
                    conversation.participantPair().low(), conversation.participantPair().high());
            case GROUP -> memberDirectory.groupRecipients(conversation.groupId());
        };
    }
}
