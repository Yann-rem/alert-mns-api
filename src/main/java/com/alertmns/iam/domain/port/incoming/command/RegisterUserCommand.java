package com.alertmns.iam.domain.port.incoming.command;

/**
 * Commande contenant les données nécessaires pour enregistrer un nouvel utilisateur.
 * Valeurs brutes — le service applicatif est responsable de la création des VO.
 */
public record RegisterUserCommand(
        String email,
        String rawPassword,
        String firstName,
        String lastName
) {}
