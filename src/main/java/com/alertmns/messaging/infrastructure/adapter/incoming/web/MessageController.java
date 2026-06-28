package com.alertmns.messaging.infrastructure.adapter.incoming.web;

import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.port.incoming.PostMessageUseCase;
import com.alertmns.messaging.domain.port.incoming.ReadConversationMessagesUseCase;
import com.alertmns.messaging.domain.port.incoming.command.ReadConversationMessagesQuery;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.MessageResponse;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.PostMessageRequest;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.PostMessageResponse;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.mapper.MessageWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messaging/conversations/{conversationId}/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Envoi de messages dans une conversation")
public class MessageController {

    private final PostMessageUseCase postMessageUseCase;
    private final ReadConversationMessagesUseCase readConversationMessagesUseCase;

    @Operation(
            summary = "Poster un message",
            description = "Envoie un message dans la conversation, éventuellement en réponse à un autre message.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Message posté"),
                    @ApiResponse(responseCode = "400", description = "Données invalides (contenu vide ou trop long, UUID invalide, réponse cible invalide)"),
                    @ApiResponse(responseCode = "403", description = "L'auteur ne participe pas à la conversation"),
                    @ApiResponse(responseCode = "404", description = "Conversation introuvable")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostMessageResponse post(
            @PathVariable UUID conversationId,
            @Valid @RequestBody PostMessageRequest request) {
        MessageId id = postMessageUseCase.post(
                MessageWebMapper.toPostMessageCommand(conversationId, request)
        );
        return new PostMessageResponse(id.value());
    }

    @Operation(
            summary = "Lister les messages",
            description = "Retourne les messages de la conversation, paginés et triés du plus récent au plus ancien.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page de messages"),
                    @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides"),
                    @ApiResponse(responseCode = "403", description = "L'utilisateur ne participe pas à la conversation"),
                    @ApiResponse(responseCode = "404", description = "Conversation introuvable")
            }
    )
    @GetMapping
    public List<MessageResponse> list(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return readConversationMessagesUseCase.read(
                        new ReadConversationMessagesQuery(conversationId.toString(), page, size))
                .stream()
                .map(MessageWebMapper::toMessageResponse)
                .toList();
    }
}
