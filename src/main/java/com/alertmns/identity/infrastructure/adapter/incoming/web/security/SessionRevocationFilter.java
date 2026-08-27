package com.alertmns.identity.infrastructure.adapter.incoming.web.security;

import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.outgoing.UserMembershipProvider;
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
 * <p><b>Deux statuts, pas un.</b> L'accès dépend de deux valeurs portées par deux contextes
 * différents et volontairement orthogonales (ADR-0019) : le statut du <em>compte</em>
 * ({@code UserStatus}, BC Identity) et celui de l'<em>adhésion</em> ({@code MemberStatus}, BC
 * Organisation). L'administration suspend l'adhésion ; le compte, lui, n'est pas touché. Vérifier
 * le seul statut de compte laisserait donc passer la suspension telle que l'interface la pratique.
 * Ce filtre est l'endroit où les deux se rejoignent, parce que l'accès à l'application suppose les
 * deux à la fois.</p>
 *
 * <p>Le statut d'adhésion arrive en {@code String} : le port le publie ainsi pour qu'Identity n'ait
 * pas à importer un type d'Organisation. La comparaison littérale est le prix de cette
 * indépendance.</p>
 *
 * <p><b>Portée.</b> Couvre la suspension d'adhésion, la suspension de compte, l'anonymisation, la
 * perte d'adhésion et la disparition du compte. Ne couvre pas le changement de rôle, qui reste
 * effectif à la reconnexion : rafraîchir les autorités demanderait de reconstruire le contexte de
 * sécurité à chaque requête.</p>
 *
 * <p><b>Effet de bord voulu.</b> L'invalidation de la session émet un événement de destruction, que
 * {@code WebSocketRevocationListener} écoute déjà : le canal temps réel se ferme donc du même
 * geste, sans que ce filtre ait à le connaître.</p>
 */
public final class SessionRevocationFilter extends OncePerRequestFilter {

    /** Valeur de {@code MemberStatus.ACTIVE}, reçue en chaîne au travers du port. */
    private static final String MEMBERSHIP_ACTIVE = "ACTIVE";

    private final UserRepository userRepository;
    private final UserMembershipProvider membershipProvider;

    public SessionRevocationFilter(UserRepository userRepository, UserMembershipProvider membershipProvider) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.membershipProvider = Objects.requireNonNull(membershipProvider, "membershipProvider must not be null");
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

    /**
     * Un compte ou une adhésion absents sont traités comme révoqués, au même titre que suspendus.
     *
     * <p>L'adhésion n'est interrogée que si le compte est utilisable : inutile de payer une seconde
     * lecture quand la première a déjà tranché.</p>
     */
    private boolean isStillActive(DomainUserDetails details) {
        boolean accountUsable = userRepository.findById(details.userId())
                .filter(user -> user.status() == UserStatus.ACTIVE)
                .filter(user -> !user.isAnonymized())
                .isPresent();

        return accountUsable && membershipProvider.findByUserId(details.userId())
                .filter(membership -> MEMBERSHIP_ACTIVE.equals(membership.status()))
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
