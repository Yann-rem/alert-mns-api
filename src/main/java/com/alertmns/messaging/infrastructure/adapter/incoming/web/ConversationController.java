package com.alertmns.messaging.infrastructure.adapter.incoming.web;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.port.incoming.CreateDirectConversationUseCase;
import com.alertmns.messaging.domain.port.incoming.ListMyConversationsUseCase;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.ConversationResponse;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.CreateDirectConversationRequest;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.dto.CreateDirectConversationResponse;
import com.alertmns.messaging.infrastructure.adapter.incoming.web.mapper.ConversationWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messaging/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Gestion des conversations")
public class ConversationController {

    private final CreateDirectConversationUseCase createDirectConversationUseCase;
    private final ListMyConversationsUseCase listMyConversationsUseCase;

    @Operation(
            summary = "Démarrer une conversation directe",
            description = "Crée (ou retourne, si elle existe) le DM 1-to-1 entre l'utilisateur courant et le membre cible.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Conversation directe créée ou déjà existante"),
                    @ApiResponse(responseCode = "400", description = "Données invalides (cible = soi-même, UUID invalide)"),
                    @ApiResponse(responseCode = "403", description = "Le membre cible est dans une autre organisation"),
                    @ApiResponse(responseCode = "404", description = "Membre cible introuvable")
            }
    )
    @PostMapping("/direct")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateDirectConversationResponse createDirect(@Valid @RequestBody CreateDirectConversationRequest request) {
        ConversationId id = createDirectConversationUseCase.create(
                ConversationWebMapper.toCreateDirectConversationCommand(request)
        );
        return new CreateDirectConversationResponse(id.value());
    }

    @Operation(
            summary = "Lister mes conversations",
            description = """
                    Retourne les conversations de l'utilisateur courant (DM et groupes), \
                    de la plus récemment active à la plus ancienne. Chaque entrée porte son titre résolu \
                    et l'aperçu de son dernier message.""",
            responses = @ApiResponse(responseCode = "200", description = "Liste des conversations")
    )
    @GetMapping
    public List<ConversationResponse> listMine() {
        return listMyConversationsUseCase.list().stream()
                .map(ConversationWebMapper::toConversationResponse)
                .toList();
    }
}
