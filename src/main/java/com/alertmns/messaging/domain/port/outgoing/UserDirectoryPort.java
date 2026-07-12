package com.alertmns.messaging.domain.port.outgoing;

import java.util.UUID;

/**
 * Port sortant d'anti-corruption vers le BC Identity, résolvant le nom d'affichage d'un utilisateur.
 *
 * <p>Résolution <strong>au runtime</strong> et <strong>anonymisation-aware</strong> (contrainte ADR-0017 §2) : le BC
 * Messaging ne dénormalise jamais le nom de l'expéditeur (il ne référence qu'un identifiant opaque), et si l'utilisateur
 * a été anonymisé — ou n'existe plus (référence orpheline, §4) — le nom résolu est
 * {@value #DELETED_USER_DISPLAY_NAME}.</p>
 */
public interface UserDirectoryPort {

    /** Nom d'affichage renvoyé pour un utilisateur anonymisé ou introuvable. */
    String DELETED_USER_DISPLAY_NAME = "Utilisateur supprimé";

    /**
     * @param userId l'identifiant de l'utilisateur
     * @return son nom d'affichage, ou {@value #DELETED_USER_DISPLAY_NAME} s'il est anonymisé ou introuvable
     */
    String displayName(UUID userId);
}
