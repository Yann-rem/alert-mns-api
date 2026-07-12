package com.alertmns.messaging.domain.port.outgoing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port sortant d'anti-corruption vers le BC Organisation, résolvant les membres en {@code userId}.
 *
 * <p>Le BC Messaging raisonne en memberId (auteur, participants) mais le canal temps réel route par {@code userId} : ce
 * port fait le pont. Doublon d'ACL assumé par BC (ISP).</p>
 */
public interface MemberDirectoryPort {

    /**
     * @param memberIdA premier participant d'une conversation directe
     * @param memberIdB second participant d'une conversation directe
     * @return les {@code userId} des deux participants
     */
    List<UUID> directRecipients(UUID memberIdA, UUID memberIdB);

    /**
     * @param groupId le groupe adossé à une conversation de groupe
     * @return les {@code userId} des membres du groupe
     */
    List<UUID> groupRecipients(UUID groupId);

    /**
     * @param memberId l'identifiant d'un membre
     * @return le {@code userId} de ce membre, ou vide s'il n'existe pas (référence orpheline)
     */
    Optional<UUID> userIdOf(UUID memberId);
}
