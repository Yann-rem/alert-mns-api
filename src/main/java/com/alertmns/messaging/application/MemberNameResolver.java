package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.port.outgoing.MemberDirectoryPort;
import com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Résout le nom d'affichage d'un membre, en composant les deux ports d'anti-corruption.
 *
 * <p>Le BC Messaging ne connaît que des {@code memberId} opaques : le nom vit dans Identity, et le
 * pont memberId → userId dans Organisation. La chaîne est composée <b>ici, dans l'application</b>,
 * pour que chaque port ACL reste mono-BC ({@link MemberDirectoryPort} vers Organisation,
 * {@link UserDirectoryPort} vers Identity).</p>
 *
 * <p>La résolution est faite <b>au runtime et jamais dénormalisée</b> (ADR-0017 §2) : un membre
 * anonymisé ou disparu devient {@value UserDirectoryPort#DELETED_USER_DISPLAY_NAME} partout, sans
 * migration de données.</p>
 *
 * <p><b>Coût assumé</b> : la résolution groupée boucle sur les identifiants <i>distincts</i>, faute
 * de méthode de lot sur les deux ports. Sur une page de messages, les auteurs distincts se comptent
 * sur les doigts d'une main ; élargir les ports pour cela serait payer une complexité inutile.</p>
 */
public final class MemberNameResolver {

    private final MemberDirectoryPort memberDirectory;
    private final UserDirectoryPort userDirectory;

    public MemberNameResolver(MemberDirectoryPort memberDirectory, UserDirectoryPort userDirectory) {
        this.memberDirectory = Objects.requireNonNull(memberDirectory, "memberDirectory must not be null");
        this.userDirectory = Objects.requireNonNull(userDirectory, "userDirectory must not be null");
    }

    /**
     * @param memberId le membre à nommer, éventuellement {@code null}
     * @return son nom d'affichage, ou {@value UserDirectoryPort#DELETED_USER_DISPLAY_NAME} s'il est
     *         anonymisé, introuvable ou absent
     */
    public String nameOf(UUID memberId) {
        if (memberId == null) {
            return UserDirectoryPort.DELETED_USER_DISPLAY_NAME;
        }
        return memberDirectory.userIdOf(memberId)
                .map(userDirectory::displayName)
                .orElse(UserDirectoryPort.DELETED_USER_DISPLAY_NAME);
    }

    /**
     * @param memberIds les membres à nommer, doublons et {@code null} tolérés
     * @return le nom de chaque identifiant distinct, jamais {@code null} en valeur
     */
    public Map<UUID, String> namesOf(Collection<UUID> memberIds) {
        Set<UUID> distinct = memberIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> names = new HashMap<>(distinct.size());
        for (UUID memberId : distinct) {
            names.put(memberId, nameOf(memberId));
        }
        return names;
    }
}
