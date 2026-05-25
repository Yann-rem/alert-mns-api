package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.identity.domain.port.incoming.RedeemActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.ValidateActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.command.RedeemActivationTokenCommand;
import com.alertmns.identity.domain.port.incoming.query.ValidateActivationTokenQuery;
import com.alertmns.identity.domain.port.incoming.result.ActivationTokenContext;
import com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto.RedeemMagicLinkRequest;
import com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto.ValidateMagicLinkResponse;
import com.alertmns.identity.infrastructure.adapter.incoming.web.auth.mapper.AuthWebMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/magic-link")
@RequiredArgsConstructor
@Tag(name = "Magic Link Activation", description = "Activation de compte par lien magique")
public class MagicLinkController {

    private final ValidateActivationTokenUseCase validateActivationTokenUseCase;
    private final RedeemActivationTokenUseCase redeemActivationTokenUseCase;

    @Operation(
            summary = "Valider un token d'activation",
            description = "Vérifie que le token est valide et non expiré, retourne le contexte utilisateur " +
                    "(email + identité) pour pré-remplir la page d'activation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token valide"),
                    @ApiResponse(responseCode = "400", description = "Token absent ou mal formé"),
                    @ApiResponse(responseCode = "410", description = "Lien d'activation invalide ou expiré")
            }
    )
    @GetMapping("/validate")
    public ValidateMagicLinkResponse validate(@RequestParam("token") String token) {
        ActivationTokenContext context = validateActivationTokenUseCase.validate(
                new ValidateActivationTokenQuery(token)
        );
        return AuthWebMapper.toValidateMagicLinkResponse(context);
    }

    @Operation(
            summary = "Consommer un token d'activation",
            description = "Active le compte utilisateur en définissant le mot de passe choisi. " +
                    "Le token est supprimé après usage et ne peut plus être réutilisé.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Compte activé"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "410", description = "Lien d'activation invalide ou expiré")
            }
    )
    @PostMapping("/redeem")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redeem(@Valid @RequestBody RedeemMagicLinkRequest request) {
        redeemActivationTokenUseCase.redeem(new RedeemActivationTokenCommand(request.token(), request.password()));
    }
}
