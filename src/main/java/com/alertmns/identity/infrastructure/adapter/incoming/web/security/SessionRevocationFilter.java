package com.alertmns.identity.infrastructure.adapter.incoming.web.security;

import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * Ferme les sessions dont le compte n'est plus actif, à la première requête qui suit.
 *
 * <p><b>Le défaut corrigé.</b> {@link DomainUserDetails} expose bien le statut du compte via
 * {@code isEnabled()} et {@code isAccountNonLocked()}, mais Spring Security ne consulte ces
 * méthodes qu'à l'authentification. Le {@code SecurityContext} est ensuite rangé dans la session
 * HTTP avec un {@code DomainUserDetails} figé au moment de la connexion : suspendre un utilisateur
 * met à jour la base sans rien changer pour les sessions déjà ouvertes, qui restent valides
 * jusqu'à leur expiration.</p>
 *
 * <p><b>Le principe retenu.</b> Recharger le statut à chaque requête authentifiée, plutôt que de
 * tenir un registre des sessions ouvertes pour les expirer activement. Le coût est une lecture par
 * requête ; le bénéfice est qu'aucun état n'est maintenu en mémoire, donc rien à reconstruire au
 * redémarrage. À l'échelle d'une école, l'arbitrage penche nettement de ce côté.</p>
 *
 * <p><b>Portée.</b> Couvre la suspension, l'anonymisation et la disparition du compte. Ne couvre
 * pas le changement de rôle, qui reste effectif à la reconnexion : les autorités proviennent du BC
 * Organisation et les recharger imposerait une seconde lecture à chaque requête.</p>
 *
 * <p><b>Effet de bord voulu.</b> L'invalidation de la session émet un événement de destruction, que
 * {@code WebSocketRevocationListener} écoute déjà : le canal temps réel se ferme donc du même
 * geste, sans que ce filtre ait à le connaître.</p>
 */
public final class SessionRevocationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public SessionRevocationFilter(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        DomainUserDetails details = authenticatedUserDetails();
        if (details != null && !isStillActive(details)) {
            revoke(request);
            // Statut nu, sans corps : même convention que le HttpStatusEntryPoint de SecurityConfig,
            // que l'intercepteur du client interprète déjà comme une session perdue.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Le principal authentifié, ou {@code null} si la requête est anonyme.
     *
     * <p>L'authentification anonyme se déclare authentifiée mais porte une chaîne pour principal :
     * le test de type suffit donc à l'écarter.</p>
     */
    private DomainUserDetails authenticatedUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getPrincipal() instanceof DomainUserDetails details ? details : null;
    }

    /** Un compte absent est traité comme révoqué, au même titre qu'un compte suspendu. */
    private boolean isStillActive(DomainUserDetails details) {
        return userRepository.findById(details.userId())
                .filter(user -> user.status() == UserStatus.ACTIVE)
                .filter(user -> !user.isAnonymized())
                .isPresent();
    }

    private void revoke(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
