package com.alertmns.messaging.infrastructure.adapter.outgoing.acl;

import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort;
import com.alertmns.shared.UserId;

import java.util.UUID;

/**
 * Adapter d'anti-corruption résolvant le nom d'affichage d'un utilisateur auprès du BC Identity.
 *
 * <p>Applique la doctrine RGPD (ADR-0017 §2/§4) : un utilisateur anonymisé ou introuvable est rendu comme
 * {@value com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort#DELETED_USER_DISPLAY_NAME}, jamais par une PII.</p>
 */
public final class UserDirectoryPortAdapter implements UserDirectoryPort {

    private final UserRepository userRepository;

    public UserDirectoryPortAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String displayName(UUID userId) {
        return userRepository.findById(UserId.from(userId))
                .map(this::render)
                .orElse(DELETED_USER_DISPLAY_NAME);
    }

    private String render(User user) {
        if (user.isAnonymized()) {
            return DELETED_USER_DISPLAY_NAME;
        }
        return user.profile().firstName().value() + " " + user.profile().lastName().value();
    }
}
