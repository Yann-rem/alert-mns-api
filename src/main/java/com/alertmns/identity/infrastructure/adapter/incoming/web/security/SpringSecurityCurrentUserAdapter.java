package com.alertmns.identity.infrastructure.adapter.incoming.web.security;

import com.alertmns.shared.AuthenticatedUser;
import com.alertmns.shared.CurrentUserPort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Adapter qui implémente {@link CurrentUserPort} en lisant le contexte de sécurité de Spring Security.
 *
 * <p>Retourne {@link Optional#empty()} dans tous les cas non-nominaux (pas d'authentification, token anonyme,
 * principal d'un type inattendu) plutôt que de propager une exception : c'est aux services métier d'exiger une
 * authentification s'ils en ont besoin.</p>
 */
public final class SpringSecurityCurrentUserAdapter implements CurrentUserPort {

    @Override
    public Optional<AuthenticatedUser> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        if (!(authentication.getPrincipal() instanceof DomainUserDetails details)) {
            return Optional.empty();
        }

        return Optional.of(new AuthenticatedUser(details.userId()));
    }
}
