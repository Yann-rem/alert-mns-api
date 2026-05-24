package com.alertmns.iam.infrastructure.adapter.incoming.web.security;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.outgoing.UserAuthoritiesProvider;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Objects;

/**
 * Adapter qui charge un {@link com.alertmns.iam.domain.model.User} depuis le {@link UserRepository}, résout ses
 * autorités via {@link UserAuthoritiesProvider}, et expose le tout à Spring Security via {@link DomainUserDetails}.
 *
 * <p>L'identifiant fonctionnel utilisé pour l'authentification est l'email.</p>
 */
public final class DomainUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserAuthoritiesProvider userAuthoritiesProvider;

    public DomainUserDetailsService(UserRepository userRepository, UserAuthoritiesProvider userAuthoritiesProvider) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.userAuthoritiesProvider = Objects.requireNonNull(
                userAuthoritiesProvider, "userAuthoritiesProvider must not be null");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        List<String> authorities = userAuthoritiesProvider.findAuthorities(user.id());
        return new DomainUserDetails(user, authorities);
    }
}
