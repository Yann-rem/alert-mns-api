package com.alertmns.messaging.infrastructure.adapter.incoming.web;

import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.port.incoming.PostMessageUseCase;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.PostMessageRequest;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.PostMessageResponse;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.mapper.MessageWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/messaging/conversations/{conversationId}/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Envoi de messages dans une conversation")
public class MessageController {

    private final PostMessageUseCase postMessageUseCase;

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
}
