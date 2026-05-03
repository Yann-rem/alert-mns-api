package com.alertmns.iam.infrastructure.adapter.incoming.web.security;

import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Adapter exposant un {@link User} du domaine IAM sous la forme attendue par Spring Security.
 *
 * <p>Conserve une référence au {@link User} pour exposer {@link #userId()} et {@link #organisationId()}, que l'adapter
 * {@code SpringSecurityCurrentUserAdapter} lira depuis le {@code Principal} sans devoir recharger l'utilisateur.</p>
 *
 * <p>Mapping des statuts :
 * <ul>
 *   <li>{@link #isEnabled()} : compte ACTIVE et non anonymisé</li>
 *   <li>{@link #isAccountNonLocked()} : compte ni SUSPENDED ni BANNED</li>
 * </ul>
 * </p>
 */
public final class DomainUserDetails implements UserDetails {

    private final User user;

    public DomainUserDetails(User user) {
        this.user = Objects.requireNonNull(user, "user must not be null");
    }

    public UserId userId() {
        return user.id();
    }

    public OrganisationId organisationId() {
        return user.organisationId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
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
