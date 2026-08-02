package com.alertmns.identity.infrastructure.adapter.outgoing.directory;

import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserJpaEntity;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import com.alertmns.organisation.domain.port.outgoing.UserDirectoryPort;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Fournit au BC <i>Organisation</i> l'identité des utilisateurs rattachés à ses membres.
 *
 * <p>Symétrique de {@code MemberUserAuthoritiesAdapter}, qui expose dans l'autre sens les autorités
 * d'un utilisateur. Aucune donnée sensible n'est exposée : le mot de passe n'est pas remonté, et
 * les comptes anonymisés portent déjà des valeurs neutralisées à la source.</p>
 */
public final class UserDirectoryAdapter implements UserDirectoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserDirectoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = Objects.requireNonNull(
                userJpaRepository, "userJpaRepository must not be null");
    }

    @Override
    public List<UserSummary> findByIds(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userJpaRepository.findByIdIn(userIds).stream()
                .map(UserDirectoryAdapter::toSummary)
                .toList();
    }

    @Override
    public List<UserSummary> findByEmails(Collection<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }
        return userJpaRepository.findByEmailIn(emails).stream()
                .map(UserDirectoryAdapter::toSummary)
                .toList();
    }

    @Override
    public List<UUID> searchIds(String term) {
        if (term == null || term.isBlank()) {
            return List.of();
        }
        return userJpaRepository.searchIds(term.trim());
    }

    private static UserSummary toSummary(UserJpaEntity entity) {
        return new UserSummary(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getStatus().name(),
                entity.isAnonymized()
        );
    }
}
