package com.alertmns.iam.infrastructure.adapter.incoming.web.security;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;

/**
 * Adapter qui charge un {@link com.alertmns.iam.domain.model.User} depuis le {@link UserRepository} et l'expose à
 * Spring Security via {@link DomainUserDetails}.
 *
 * <p>L'identifiant fonctionnel utilisé pour l'authentification est l'email.</p>
 */
public final class DomainUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(Email.of(email))
                .map(DomainUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
