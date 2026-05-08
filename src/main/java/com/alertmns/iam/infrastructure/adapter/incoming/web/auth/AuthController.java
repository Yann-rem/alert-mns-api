package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.iam.infrastructure.adapter.incoming.web.auth.dto.LoginRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.auth.dto.MeResponse;
import com.alertmns.iam.infrastructure.adapter.incoming.web.auth.mapper.AuthWebMapper;
import com.alertmns.shared.AuthenticatedUser;
import com.alertmns.shared.CurrentUserPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentification (login, logout, identité courante)")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CurrentUserPort currentUserPort;
    private final UserRepository userRepository;

    @Operation(
            summary = "Authentifier un utilisateur",
            description = "Vérifie les credentials et crée une session côté serveur. " +
                    "Le cookie JSESSIONID est posé dans la réponse.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentification réussie"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "401", description = "Credentials invalides"),
                    @ApiResponse(responseCode = "403", description = "Compte désactivé ou verrouillé")
            }
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());

        Authentication authentication = authenticationManager.authenticate(token);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }

    @Operation(
            summary = "Déconnecter l'utilisateur courant",
            description = "Invalide la session côté serveur et supprime le cookie JSESSIONID. " +
                    "Idempotent : retourne 204 même sans session active.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Session invalidée")
            }
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest) throws ServletException {
        httpRequest.logout();
    }

    @Operation(
            summary = "Récupérer l'identité de l'utilisateur courant",
            description = "Retourne les informations de l'utilisateur authentifié sur la base de la session courante.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Identité retournée"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié")
            }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public MeResponse me() {
        AuthenticatedUser current = currentUserPort.currentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user in current context"));

        User user = userRepository
                .findById(current.userId())
                .orElseThrow(() -> new UserNotFoundException(current.userId()));

        return AuthWebMapper.toMeResponse(user);
    }
}
