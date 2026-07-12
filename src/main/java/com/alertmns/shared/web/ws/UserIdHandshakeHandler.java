package com.alertmns.shared.web.ws;

import com.alertmns.shared.CurrentUserPort;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Détermine l'identité de la session WebSocket comme étant le {@code userId} de l'utilisateur authentifié.
 *
 * <p>Le handshake {@code /ws} est une requête HTTP déjà passée par Spring Security : le {@link CurrentUserPort} lit le
 * contexte de sécurité (thread-bound) et fournit le {@code userId}. Nommer la session par {@code userId} (et non par
 * l'email, nom par défaut du principal) permet de router les <em>user-destinations</em> ({@code /user/{userId}/queue/…})
 * sur la clé stable et opaque utilisée partout dans le système — l'email, lui, change à l'anonymisation.</p>
 *
 * <p>Reste agnostique des Bounded Contexts : il ne dépend que de l'abstraction {@code shared}, pas de Spring Security
 * ni du BC Identity.</p>
 */
public class UserIdHandshakeHandler extends DefaultHandshakeHandler {

    private final CurrentUserPort currentUserPort;

    public UserIdHandshakeHandler(CurrentUserPort currentUserPort) {
        this.currentUserPort = currentUserPort;
    }

    @Override
    protected Principal determineUser(
            ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return currentUserPort.currentUser()
                .<Principal>map(user -> new UserIdPrincipal(user.userId().value().toString()))
                .orElseGet(() -> super.determineUser(request, wsHandler, attributes));
    }

    /**
     * Principal minimal dont le nom est le {@code userId} : c'est ce nom que Spring utilise pour router les
     * user-destinations.
     */
    private record UserIdPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
