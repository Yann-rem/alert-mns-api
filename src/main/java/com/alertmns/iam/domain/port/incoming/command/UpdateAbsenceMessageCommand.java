package com.alertmns.iam.domain.port.incoming.command;

/**
 * Commande contenant les données nécessaires pour la mise à jour du message d'absence d'un utilisateur.
 * Valeurs brutes — le service applicatif est responsable de la création des VO.
 */
public record UpdateAbsenceMessageCommand(String userId, String content, boolean active) {}
