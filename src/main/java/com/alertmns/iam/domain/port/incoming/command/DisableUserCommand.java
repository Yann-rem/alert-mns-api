package com.alertmns.iam.domain.port.incoming.command;

/**
 * Commande contenant les données nécessaires pour désactiver un utilisateur.
 * Valeur brute — le service applicatif est responsable de la création du VO.
 */
public record DisableUserCommand(String userId) {}
