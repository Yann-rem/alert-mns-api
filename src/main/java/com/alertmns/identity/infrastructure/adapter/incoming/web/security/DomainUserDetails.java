package com.alertmns.identity.infrastructure.adapter.incoming.web.security;

import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.shared.UserId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Adapter exposant un {@link User} du domaine Identity sous la forme attendue par Spring Security.
 *
 * <p>Conserve une référence au {@link User} pour exposer {@link #userId()}, que l'adapter
 * {@code SpringSecurityCurrentUserAdapter} lira depuis le {@code Principal} sans devoir recharger l'utilisateur.</p>
 *
 * <p><b>Autorités</b> : injectées à la construction sous forme de noms résolus en amont par un
 * {@code UserAuthoritiesProvider}. La source de vérité des rôles est le BC Organisation ({@code Member.role}).</p>
 *
 * <p>Mapping des statuts :</p>
 * <ul>
 *   <li>{@link #isEnabled()} : compte ACTIVE et non anonymisé</li>
 *   <li>{@link #isAccountNonLocked()} : compte ni SUSPENDED ni BANNED</li>
 * </ul>
 */
public final class DomainUserDetails implements UserDetails {

    private final User user;
    private final Collection<GrantedAuthority> authorities;

    public DomainUserDetails(User user, List<String> authorityNames) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(authorityNames, "authorityNames must not be null");
        this.authorities = authorityNames.stream()
                .map(name -> (GrantedAuthority) new SimpleGrantedAuthority(name))
                .toList();
    }

    public UserId userId() {
        return user.id();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.hashedPassword().value();
    }

    @Override
    public String getUsername() {
        return user.email().value();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.status() != UserStatus.SUSPENDED && user.status() != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.status() == UserStatus.ACTIVE && !user.isAnonymized();
    }
}
