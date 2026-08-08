package com.alertmns.alerting.domain.port.outgoing;

import java.util.UUID;

/**
 * Port sortant d'anti-corruption résolvant le nom du groupe ciblé par une alerte.
 *
 * <p>Même raison d'être que {@link IssuerDirectoryPort} : la liste des groupes est réservée aux rôles ADMIN et
 * MANAGER, un simple membre ne peut donc pas traduire lui-même le {@code groupId} porté par une alerte de groupe.</p>
 *
 * <p>Une référence orpheline (groupe supprimé) ne doit pas faire échouer la lecture de l'alerte : elle est rendue
 * comme {@value #DELETED_GROUP_DISPLAY_NAME}.</p>
 */
public interface GroupDirectoryPort {

    /** Nom renvoyé pour un groupe supprimé ou introuvable. */
    String DELETED_GROUP_DISPLAY_NAME = "Groupe supprimé";

    /**
     * @param groupId l'identifiant du groupe ciblé, éventuellement {@code null}
     * @return son nom, ou {@value #DELETED_GROUP_DISPLAY_NAME} s'il n'existe plus
     */
    String nameOf(UUID groupId);
}
