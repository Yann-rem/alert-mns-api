package com.alertmns.iam.domain.port.incoming.command;

/**
 * Commande contenant les données nécessaires pour activer un utilisateur.
 * Valeur brute — le service applicatif est responsable de la création du VO.
 */
public record ActivateUserCommand(String userId) {}
