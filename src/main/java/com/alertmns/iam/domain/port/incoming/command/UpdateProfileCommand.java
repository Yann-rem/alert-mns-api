package com.alertmns.iam.domain.port.incoming.command;

/**
 * Commande contenant les données nécessaires pour la mise à jour du profil d'un utilisateur.
 * Valeurs brutes — le service applicatif est responsable de la création des VO.
 */
public record UpdateProfileCommand(
        String userId,
        String firstName,
        String lastName,
        String avatar
) {}
